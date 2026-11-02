package com.courseshare.category.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CategoryStatusRequest(
        @NotNull(message = "请提供分类状态")
        @Min(value = 0, message = "分类状态不合法")
        @Max(value = 1, message = "分类状态不合法")
        Integer status,
        @NotNull(message = "请提供数据版本")
        Integer version
) {
}

