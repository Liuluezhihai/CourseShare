package com.courseshare.resource.dto;

import jakarta.validation.constraints.NotNull;

public record AuditVersionItem(
        @NotNull(message = "资源 ID 不能为空") Long id,
        @NotNull(message = "资源版本不能为空") Integer version
) {
}

