package com.courseshare.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceSaveRequest(
        @NotBlank(message = "资源标题不能为空")
        @Size(max = 200, message = "资源标题不能超过 200 个字符")
        String title,
        @NotBlank(message = "资源摘要不能为空")
        @Size(max = 500, message = "资源摘要不能超过 500 个字符")
        String summary,
        @NotBlank(message = "资源正文不能为空")
        @Size(max = 1_000_000, message = "资源正文过长")
        String content,
        @NotNull(message = "请选择资源分类")
        Long categoryId,
        Integer version
) {
}

