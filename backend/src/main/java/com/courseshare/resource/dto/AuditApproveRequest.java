package com.courseshare.resource.dto;

import jakarta.validation.constraints.NotNull;

public record AuditApproveRequest(
        @NotNull(message = "请提供数据版本") Integer version
) {
}

