package com.courseshare.resource.service;

import com.courseshare.category.service.CategoryService;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.resource.entity.CmsArticle;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.model.ResourceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private CmsArticleMapper articleMapper;
    @Mock
    private CategoryService categoryService;
    @Mock
    private FileService fileService;
    @InjectMocks
    private ResourceService resourceService;

    @Test
    void submitMovesDraftToPending() {
        CmsArticle article = article(7L, 3L, ResourceStatus.DRAFT, 2);
        when(articleMapper.selectById(7L)).thenReturn(article);
        when(articleMapper.updateById(any(CmsArticle.class))).thenReturn(1);

        resourceService.submit(7L, 3L, 2);

        assertEquals(ResourceStatus.PENDING, article.getStatus());
    }

    @Test
    void authorCannotSubmitAnotherUsersResource() {
        when(articleMapper.selectById(7L)).thenReturn(article(7L, 9L, ResourceStatus.DRAFT, 2));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resourceService.submit(7L, 3L, 2)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    void staleVersionProducesConflict() {
        when(articleMapper.selectById(7L)).thenReturn(article(7L, 3L, ResourceStatus.DRAFT, 2));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resourceService.submit(7L, 3L, 1)
        );

        assertEquals(ErrorCode.VERSION_CONFLICT, exception.getErrorCode());
    }

    @Test
    void approvePublishesPendingResource() {
        CmsArticle article = article(7L, 3L, ResourceStatus.PENDING, 5);
        when(articleMapper.selectById(7L)).thenReturn(article);
        when(articleMapper.updateById(any(CmsArticle.class))).thenReturn(1);

        resourceService.approve(7L, 5, 2L);

        assertEquals(ResourceStatus.PUBLISHED, article.getStatus());
        assertEquals(2L, article.getAuditBy());
        assertEquals(true, article.getPublishedAt() != null);
    }

    private CmsArticle article(Long id, Long authorId, int status, int version) {
        CmsArticle article = new CmsArticle();
        article.setId(id);
        article.setAuthorId(authorId);
        article.setStatus(status);
        article.setVersion(version);
        article.setDeleted(0);
        article.setAttachmentPath("2026/01/file.pdf");
        article.setAttachmentName("file.pdf");
        return article;
    }
}

