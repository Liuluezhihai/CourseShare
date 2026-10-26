package com.courseshare.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategorySaveRequest(
        Long parentId,
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过 50 个字符")
        String name,
        @Size(max = 255, message = "分类说明不能超过 255 个字符")
        String description,
        @Min(value = 0, message = "排序值不能为负数")
        Integer sortOrder,
        Integer status,
        Integer version
) {
}

