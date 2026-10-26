package com.courseshare.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.courseshare.category.dto.CategorySaveRequest;
import com.courseshare.category.entity.CmsCategory;
import com.courseshare.category.mapper.CategoryUsageMapper;
import com.courseshare.category.mapper.CmsCategoryMapper;
import com.courseshare.category.vo.CategoryTreeNode;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CategoryService {

    private final CmsCategoryMapper categoryMapper;
    private final CategoryUsageMapper usageMapper;

    public CategoryService(CmsCategoryMapper categoryMapper, CategoryUsageMapper usageMapper) {
        this.categoryMapper = categoryMapper;
        this.usageMapper = usageMapper;
    }

    public List<CategoryTreeNode> publicTree() {
        List<CmsCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getStatus, 1)
                .orderByAsc(CmsCategory::getSortOrder)
                .orderByAsc(CmsCategory::getId));
        return buildTree(categories, false, false);
    }

    public List<CategoryTreeNode> adminTree() {
        List<CmsCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                .orderByAsc(CmsCategory::getLevel)
                .orderByAsc(CmsCategory::getSortOrder)
                .orderByAsc(CmsCategory::getId));
        return buildTree(categories, true, true);
    }

    public CmsCategory requireEnabled(Long id) {
        CmsCategory category = requireExisting(id);
        if (!Integer.valueOf(1).equals(category.getStatus())) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "分类已停用");
        }
        if (category.getParentId() != null && !Integer.valueOf(1).equals(requireExisting(category.getParentId()).getStatus())) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "父分类已停用");
        }
        return category;
    }

    public CmsCategory requireExisting(Long id) {
        CmsCategory category = categoryMapper.selectById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    public Set<Long> descendantIds(Long rootId, boolean enabledOnly) {
        List<CmsCategory> all = categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>());
        Map<Long, List<CmsCategory>> children = new HashMap<>();
        for (CmsCategory category : all) {
            children.computeIfAbsent(category.getParentId(), key -> new ArrayList<>()).add(category);
        }
        Set<Long> result = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            for (CmsCategory child : children.getOrDefault(current, List.of())) {
                if (!enabledOnly || Integer.valueOf(1).equals(child.getStatus())) {
                    result.add(child.getId());
                }
                queue.addLast(child.getId());
            }
        }
        return result;
    }

    public Set<Long> enabledCategoryIds() {
        return categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                        .eq(CmsCategory::getStatus, 1))
                .stream()
                .map(CmsCategory::getId)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Transactional
    public CmsCategory create(CategorySaveRequest request) {
        CmsCategory parent = null;
        int level = 1;
        if (request.parentId() != null) {
            parent = requireExisting(request.parentId());
            level = parent.getLevel() + 1;
            if (level > 3) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类最多支持三级");
            }
        }
        String name = request.name().trim();
        ensureUniqueName(request.parentId(), name, null);
        int status = request.status() == null ? 1 : request.status();
        if (parent != null && !Integer.valueOf(1).equals(parent.getStatus()) && status == 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "父分类停用时不能创建启用子分类");
        }

        CmsCategory category = new CmsCategory();
        category.setParentId(request.parentId());
        category.setName(name);
        category.setLevel(level);
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(status);
        category.setDescription(request.description());
        category.setVersion(0);
        category.setDeleted(0);
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public CmsCategory update(Long id, CategorySaveRequest request) {
        CmsCategory existing = requireExisting(id);
        if (request.version() == null || !request.version().equals(existing.getVersion())) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类不能作为自己的父分类");
        }

        List<CmsCategory> all = categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>());
        Map<Long, CmsCategory> byId = new HashMap<>();
        all.forEach(category -> byId.put(category.getId(), category));
        validateNoCycle(id, request.parentId(), byId);

        int newLevel = 1;
        CmsCategory parent = null;
        if (request.parentId() != null) {
            parent = byId.get(request.parentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "父分类不存在");
            }
            newLevel = parent.getLevel() + 1;
        }
        int maxDescendantDepth = maxDescendantDepth(id, byId);
        if (newLevel + maxDescendantDepth > 3) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "移动后分类层级将超过三级");
        }

        String name = request.name().trim();
        ensureUniqueName(request.parentId(), name, id);
        int newStatus = request.status() == null ? existing.getStatus() : request.status();
        if (parent != null && !Integer.valueOf(1).equals(parent.getStatus()) && newStatus == 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "父分类停用时不能启用子分类");
        }

        int oldLevel = existing.getLevel();
        if (oldLevel != newLevel) {
            int delta = newLevel - oldLevel;
            Set<Long> descendants = descendantIds(id, false);
            for (Long descendantId : descendants) {
                CmsCategory descendant = byId.get(descendantId);
                if (descendant != null) {
                    descendant.setLevel(descendant.getLevel() + delta);
                    categoryMapper.updateById(descendant);
                }
            }
        }

        existing.setParentId(request.parentId());
        existing.setName(name);
        existing.setLevel(newLevel);
        existing.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        existing.setDescription(request.description());
        int beforeStatus = existing.getStatus();
        existing.setStatus(newStatus);
        if (categoryMapper.updateById(existing) == 0) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
        if (beforeStatus == 1 && newStatus == 0) {
            disableDescendants(id);
        }
        return categoryMapper.selectById(id);
    }

    @Transactional
    public void changeStatus(Long id, int status, int version) {
        CmsCategory existing = requireExisting(id);
        if (!Integer.valueOf(version).equals(existing.getVersion())) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
        if (status == 1) {
            if (existing.getParentId() != null) {
                requireEnabled(existing.getParentId());
            }
        }
        existing.setStatus(status);
        if (categoryMapper.updateById(existing) == 0) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
        if (status == 0) {
            disableDescendants(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        CmsCategory existing = requireExisting(id);
        Long children = categoryMapper.selectCount(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getParentId, id));
        if (children > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "请先删除子分类");
        }
        if (usageMapper.countResources(id) > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "分类下仍有资源，不能删除");
        }
        categoryMapper.deleteById(existing.getId());
    }

    private void disableDescendants(Long id) {
        Set<Long> descendants = descendantIds(id, false);
        if (!descendants.isEmpty()) {
            categoryMapper.update(null, new LambdaUpdateWrapper<CmsCategory>()
                    .in(CmsCategory::getId, descendants)
                    .eq(CmsCategory::getStatus, 1)
                    .set(CmsCategory::getStatus, 0)
                    .setSql("version = version + 1"));
        }
    }

    private void ensureUniqueName(Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<CmsCategory> query = new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getName, name);
        if (parentId == null) {
            query.isNull(CmsCategory::getParentId);
        } else {
            query.eq(CmsCategory::getParentId, parentId);
        }
        if (excludeId != null) {
            query.ne(CmsCategory::getId, excludeId);
        }
        if (categoryMapper.selectCount(query) > 0) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "同级分类名称不能重复");
        }
    }

    private void validateNoCycle(Long id, Long newParentId, Map<Long, CmsCategory> byId) {
        Long current = newParentId;
        while (current != null) {
            if (current.equals(id)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类父子关系形成循环");
            }
            CmsCategory category = byId.get(current);
            current = category == null ? null : category.getParentId();
        }
    }

    private int maxDescendantDepth(Long id, Map<Long, CmsCategory> byId) {
        Map<Long, List<CmsCategory>> children = new HashMap<>();
        byId.values().forEach(category -> children
                .computeIfAbsent(category.getParentId(), key -> new ArrayList<>())
                .add(category));
        return maxDepth(id, children, 0);
    }

    private int maxDepth(Long id, Map<Long, List<CmsCategory>> children, int currentDepth) {
        List<CmsCategory> descendants = children.getOrDefault(id, List.of());
        int max = currentDepth;
        for (CmsCategory descendant : descendants) {
            max = Math.max(max, maxDepth(descendant.getId(), children, currentDepth + 1));
        }
        return max;
    }

    private List<CategoryTreeNode> buildTree(List<CmsCategory> categories, boolean includeDisabled, boolean withCounts) {
        Map<Long, Long> counts = new HashMap<>();
        if (withCounts) {
            for (Map<String, Object> row : usageMapper.countPublishedByCategory()) {
                Object id = row.get("categoryId");
                Object count = row.get("resourceCount");
                if (id instanceof Number idNumber && count instanceof Number countNumber) {
                    counts.put(idNumber.longValue(), countNumber.longValue());
                }
            }
        }
        Map<Long, CategoryTreeNode> nodes = new LinkedHashMap<>();
        categories.stream()
                .filter(category -> includeDisabled || Integer.valueOf(1).equals(category.getStatus()))
                .sorted(Comparator.comparing(CmsCategory::getSortOrder).thenComparing(CmsCategory::getId))
                .forEach(category -> {
                    CategoryTreeNode node = new CategoryTreeNode();
                    node.setId(category.getId());
                    node.setParentId(category.getParentId());
                    node.setName(category.getName());
                    node.setLevel(category.getLevel());
                    node.setSortOrder(category.getSortOrder());
                    node.setStatus(category.getStatus());
                    node.setDescription(category.getDescription());
                    node.setVersion(category.getVersion());
                    node.setResourceCount(counts.getOrDefault(category.getId(), 0L));
                    nodes.put(category.getId(), node);
                });

        List<CategoryTreeNode> roots = new ArrayList<>();
        for (CategoryTreeNode node : nodes.values()) {
            if (node.getParentId() == null || !nodes.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodes.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }
}

