package com.courseshare.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long resourceId;
    private Long userId;
    private String nickname;
    private Integer score;
    private String content;
    private Long likeCount;
    private Boolean likedByMe;
    private Boolean mine;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
