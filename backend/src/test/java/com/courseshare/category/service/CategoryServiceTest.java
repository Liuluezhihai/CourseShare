package com.courseshare.category.service;

import com.courseshare.category.dto.CategorySaveRequest;
import com.courseshare.category.entity.CmsCategory;
import com.courseshare.category.mapper.CategoryUsageMapper;
import com.courseshare.category.mapper.CmsCategoryMapper;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CmsCategoryMapper categoryMapper;
    @Mock
    private CategoryUsageMapper usageMapper;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void rejectsFourthLevelCategory() {
        CmsCategory parent = category(1L, 3, 1);
        when(categoryMapper.selectById(1L)).thenReturn(parent);
        CategorySaveRequest request = new CategorySaveRequest(1L, "第四级", null, 0, 1, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(request)
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void rejectsEnabledChildUnderDisabledParent() {
        CmsCategory parent = category(1L, 1, 0);
        when(categoryMapper.selectById(1L)).thenReturn(parent);
        when(categoryMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(0L);
        CategorySaveRequest request = new CategorySaveRequest(1L, "禁用父分类下的子分类", null, 0, 1, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(request)
        );

        assertEquals(ErrorCode.STATE_CONFLICT, exception.getErrorCode());
    }

    private CmsCategory category(Long id, int level, int status) {
        CmsCategory category = new CmsCategory();
        category.setId(id);
        category.setLevel(level);
        category.setStatus(status);
        category.setDeleted(0);
        category.setVersion(0);
        return category;
    }
}

