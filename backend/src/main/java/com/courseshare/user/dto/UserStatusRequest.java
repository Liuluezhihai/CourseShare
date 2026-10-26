package com.courseshare.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull(message = "请提供账号状态")
        @Min(value = 0, message = "账号状态不合法")
        @Max(value = 1, message = "账号状态不合法")
        Integer status
) {
}

