package com.courseshare.auth.model;

import java.util.Set;

public record CurrentUser(
        Long id,
        String username,
        String nickname,
        String email,
        Set<String> roles,
        Set<String> permissions
) {
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}

