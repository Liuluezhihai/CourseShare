package com.courseshare.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserListItemVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<String> roleCodes = new ArrayList<>();
    private List<String> roleNames = new ArrayList<>();
}

