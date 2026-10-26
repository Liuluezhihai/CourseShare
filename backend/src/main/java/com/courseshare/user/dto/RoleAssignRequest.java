package com.courseshare.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleAssignRequest(
        @NotEmpty(message = "用户至少需要一个角色")
        @Size(max = 10, message = "角色数量过多")
        List<String> roleCodes
) {
}

