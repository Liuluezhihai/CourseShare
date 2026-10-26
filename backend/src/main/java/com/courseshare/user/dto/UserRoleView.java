package com.courseshare.user.dto;

import lombok.Data;

@Data
public class UserRoleView {
    private Long userId;
    private String roleCode;
    private String roleName;
}

