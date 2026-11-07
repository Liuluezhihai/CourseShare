package com.courseshare.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.category.entity.CmsCategory;
import com.courseshare.category.service.CategoryService;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.resource.dto.AuditVersionItem;
import com.courseshare.resource.dto.BatchPublishRequest;
import com.courseshare.resource.dto.ResourceQuery;
import com.courseshare.resource.dto.ResourceSaveRequest;
import com.courseshare.resource.entity.CmsArticle;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.model.ResourceStatus;
import com.courseshare.resource.vo.ResourceDetailVO;
import com.courseshare.resource.vo.ResourceListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ResourceService {

    private static final Set<String> PUBLIC_SORTS = Set.of("publishedAt", "viewCount", "downloadCount");
    private static final Set<String> ADMIN_SORTS = Set.of(
            "publishedAt", "viewCount", "downloadCount", "updatedAt", "createdAt"
    );

    private final CmsArticleMapper articleMapper;
    private final CategoryService categoryService;
    private final FileService fileService;

    public ResourceService(
            CmsArticleMapper articleMapper,
            CategoryService categoryService,
            FileService fileService) {
        this.articleMapper = articleMapper;
        this.categoryService = categoryService;
        this.fileService = fileService;
    }

    public PageResult<ResourceListItemVO> publicPage(ResourceQuery query) {
        normalizeQuery(query, true);
        Set<Long> categoryIds = null;
        if (query.getCategoryId() != null) {
            categoryService.requireEnabled(query.getCategoryId());
            categoryIds = descendantScope(query.getCategoryId(), true);
        }
        Page<ResourceListItemVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        IPage<ResourceListItemVO> result = articleMapper.selectResourcePage(
                page, query, categoryIds, true
        );
        return PageResult.from(result);
    }

    public PageResult<ResourceListItemVO> authorPage(ResourceQuery query, Long authorId) {
        normalizeQuery(query, false);
        query.setAuthorId(authorId);
        Set<Long> categoryIds = query.getCategoryId() == null
                ? null
                : descendantScope(query.getCategoryId(), false);
        Page<ResourceListItemVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        return PageResult.from(articleMapper.selectResourcePage(page, query, categoryIds, false));
    }

    public PageResult<ResourceListItemVO> adminPage(ResourceQuery query) {
        normalizeQuery(query, false);
        Set<Long> categoryIds = query.getCategoryId() == null
                ? null
                : descendantScope(query.getCategoryId(), false);
        Page<ResourceListItemVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        return PageResult.from(articleMapper.selectResourcePage(page, query, categoryIds, false));
    }

    public ResourceDetailVO publicDetail(Long id) {
        ResourceDetailVO detail = articleMapper.selectResourceDetail(id, true);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在或尚未发布");
        }
        articleMapper.incrementViewCount(id);
        detail.setViewCount(detail.getViewCount() + 1);
        return detail;
    }

    public ResourceDetailVO authorDetail(Long id, Long authorId) {
        CmsArticle article = requireOwned(id, authorId);
        return articleMapper.selectResourceDetail(article.getId(), false);
    }

    public ResourceDetailVO adminDetail(Long id) {
        CmsArticle article = requireExisting(id);
        return articleMapper.selectResourceDetail(article.getId(), false);
    }

    @Transactional
    public ResourceDetailVO create(ResourceSaveRequest request, MultipartFile file, Long authorId) {
        CmsCategory category = categoryService.requireEnabled(request.categoryId());
        FileService.StoredFile stored = fileService.store(file);
        if (stored != null) {
            registerRollbackCleanup(stored.relativePath());
        }

        CmsArticle article = new CmsArticle();
        article.setTitle(request.title().trim());
        article.setSummary(request.summary().trim());
        article.setContent(request.content());
        article.setCategoryId(category.getId());
        article.setAuthorId(authorId);
        applyAttachment(article, stored);
        article.setStatus(ResourceStatus.DRAFT);
        article.setViewCount(0L);
        article.setDownloadCount(0L);
        article.setVersion(0);
        article.setDeleted(0);
        articleMapper.insert(article);
        return articleMapper.selectResourceDetail(article.getId(), false);
    }

    @Transactional
    public ResourceDetailVO update(Long id, ResourceSaveRequest request, MultipartFile file, Long authorId) {
        CmsArticle article = requireOwned(id, authorId);
        requireEditable(article);
        requireVersion(article, request.version());
        CmsCategory category = categoryService.requireEnabled(request.categoryId());
        FileService.StoredFile stored = fileService.store(file);
        String oldPath = article.getAttachmentPath();
        if (stored != null) {
            registerRollbackCleanup(stored.relativePath());
        }

        article.setTitle(request.title().trim());
        article.setSummary(request.summary().trim());
        article.setContent(request.content());
        article.setCategoryId(category.getId());
        if (stored != null) {
            applyAttachment(article, stored);
        }
        if (articleMapper.updateById(article) == 0) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
        if (stored != null && oldPath != null) {
            registerAfterCommit(() -> fileService.delete(oldPath));
        }
        return articleMapper.selectResourceDetail(id, false);
    }

    @Transactional
    public void submit(Long id, Long authorId, Integer version) {
        CmsArticle article = requireOwned(id, authorId);
        requireStatus(article, ResourceStatus.SUBMITTABLE, "当前状态不能提交审核");
        requireVersion(article, version);
        article.setStatus(ResourceStatus.PENDING);
        updateOrConflict(article);
    }

    @Transactional
    public void withdraw(Long id, Long authorId, Integer version) {
        CmsArticle article = requireOwned(id, authorId);
        requireStatus(article, Set.of(ResourceStatus.PENDING), "只有待审核资源可以撤回");
        requireVersion(article, version);
        article.setStatus(ResourceStatus.DRAFT);
        updateOrConflict(article);
    }

    @Transactional
    public void authorOffline(Long id, Long authorId, Integer version) {
        CmsArticle article = requireOwned(id, authorId);
        requireStatus(article, Set.of(ResourceStatus.PUBLISHED), "只有已发布资源可以下架");
        requireVersion(article, version);
        article.setStatus(ResourceStatus.OFFLINE);
        updateOrConflict(article);
    }

    @Transactional
    public void authorDelete(Long id, Long authorId) {
        CmsArticle article = requireOwned(id, authorId);
        requireStatus(article, ResourceStatus.DELETABLE, "当前状态不能删除资源");
        articleMapper.deleteById(id);
        registerAfterCommit(() -> fileService.delete(article.getAttachmentPath()));
    }

    @Transactional
    public void approve(Long id, Integer version, Long auditBy) {
        CmsArticle article = requireExisting(id);
        requireStatus(article, Set.of(ResourceStatus.PENDING), "只有待审核资源可以审核");
        requireVersion(article, version);
        LocalDateTime now = LocalDateTime.now();
        article.setStatus(ResourceStatus.PUBLISHED);
        article.setAuditBy(auditBy);
        article.setAuditAt(now);
        article.setAuditRemark("");
        article.setPublishedAt(now);
        updateOrConflict(article);
    }

    @Transactional
    public void reject(Long id, Integer version, String remark, Long auditBy) {
        CmsArticle article = requireExisting(id);
        requireStatus(article, Set.of(ResourceStatus.PENDING), "只有待审核资源可以驳回");
        requireVersion(article, version);
        article.setStatus(ResourceStatus.REJECTED);
        article.setAuditBy(auditBy);
        article.setAuditAt(LocalDateTime.now());
        article.setAuditRemark(remark.trim());
        updateOrConflict(article);
    }

    @Transactional
    public void adminOffline(Long id, Integer version) {
        CmsArticle article = requireExisting(id);
        requireStatus(article, Set.of(ResourceStatus.PUBLISHED), "只有已发布资源可以下架");
        requireVersion(article, version);
        article.setStatus(ResourceStatus.OFFLINE);
        updateOrConflict(article);
    }

    @Transactional
    public void adminDelete(Long id) {
        CmsArticle article = requireExisting(id);
        requireStatus(article, ResourceStatus.DELETABLE, "请先下架或完成待审核流程再删除");
        articleMapper.deleteById(id);
        registerAfterCommit(() -> fileService.delete(article.getAttachmentPath()));
    }

    @Transactional
    public int batchPublish(BatchPublishRequest request, Long auditBy) {
        Set<Long> ids = new HashSet<>();
        for (AuditVersionItem item : request.items()) {
            if (!ids.add(item.id())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "批量发布列表中存在重复资源");
            }
        }
        int updated = articleMapper.batchPublish(request.items(), auditBy, LocalDateTime.now());
        if (updated != request.items().size()) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "部分资源状态或版本已经变化，请刷新后重试");
        }
        return updated;
    }

    public FileService.DownloadFile prepareDownload(Long id) {
        CmsArticle article = requireExisting(id);
        requireStatus(article, Set.of(ResourceStatus.PUBLISHED), "资源尚未发布");
        categoryService.requireEnabled(article.getCategoryId());
        return fileService.resolveForDownload(
                article.getAttachmentPath(),
                article.getAttachmentName(),
                article.getAttachmentMime()
        );
    }

    public FileService.DownloadFile prepareAdminDownload(Long id) {
        CmsArticle article = requireExisting(id);
        return fileService.resolveForDownload(
                article.getAttachmentPath(),
                article.getAttachmentName(),
                article.getAttachmentMime()
        );
    }

    public void recordDownload(Long id) {
        articleMapper.incrementDownloadCount(id);
    }

    private CmsArticle requireOwned(Long id, Long authorId) {
        CmsArticle article = requireExisting(id);
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能操作自己的资源");
        }
        return article;
    }

    private CmsArticle requireExisting(Long id) {
        CmsArticle article = articleMapper.selectById(id);
        if (article == null || Integer.valueOf(1).equals(article.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return article;
    }

    private void requireEditable(CmsArticle article) {
        requireStatus(article, ResourceStatus.EDITABLE, "当前状态不能编辑资源");
    }

    private void requireStatus(CmsArticle article, Set<Integer> expected, String message) {
        if (!expected.contains(article.getStatus())) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, message);
        }
    }

    private void requireVersion(CmsArticle article, Integer version) {
        if (version == null || !version.equals(article.getVersion())) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
    }

    private void updateOrConflict(CmsArticle article) {
        if (articleMapper.updateById(article) == 0) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
    }

    private void applyAttachment(CmsArticle article, FileService.StoredFile stored) {
        if (stored == null) {
            return;
        }
        article.setAttachmentName(stored.originalName());
        article.setAttachmentPath(stored.relativePath());
        article.setAttachmentExt(stored.extension());
        article.setAttachmentMime(stored.mime());
        article.setAttachmentSize(stored.size());
    }

    private void normalizeQuery(ResourceQuery query, boolean publicOnly) {
        String keyword = query.getKeyword();
        query.setKeyword(keyword == null || keyword.isBlank()
                ? null
                : keyword.trim()
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_"));
        Set<String> allowed = publicOnly ? PUBLIC_SORTS : ADMIN_SORTS;
        if (!allowed.contains(query.getSortBy())) {
            query.setSortBy("publishedAt");
        }
        String direction = query.getSortDirection() == null
                ? "desc"
                : query.getSortDirection().toLowerCase(Locale.ROOT);
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            direction = "desc";
        }
        if (!"publishedAt".equals(query.getSortBy()) && "asc".equals(direction)) {
            direction = "desc";
        }
        query.setSortDirection(direction);
    }

    private Set<Long> descendantScope(Long rootId, boolean enabledOnly) {
        Set<Long> ids = new HashSet<>(categoryService.descendantIds(rootId, enabledOnly));
        ids.add(rootId);
        return ids;
    }

    private void registerRollbackCleanup(String path) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        fileService.delete(path);
                    }
                }
            });
        }
    }

    private void registerAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}

