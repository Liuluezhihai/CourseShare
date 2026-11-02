package com.courseshare.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuditRejectRequest(
        @NotNull(message = "请提供数据版本") Integer version,
        @NotBlank(message = "驳回原因不能为空")
        @Size(max = 500, message = "驳回原因不能超过 500 个字符")
        String remark
) {
}

