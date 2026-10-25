package com.courseshare.auth.vo;

import java.util.Set;

public record CurrentUserVO(
        Long id,
        String username,
        String nickname,
        String email,
        Set<String> roles,
        Set<String> permissions
) {
}

