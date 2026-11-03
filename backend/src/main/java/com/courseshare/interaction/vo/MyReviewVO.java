package com.courseshare.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyReviewVO {
    private Long id;
    private Long resourceId;
    private String resourceTitle;
    private Integer resourceStatus;
    private Boolean resourceDeleted;
    private Integer score;
    private String content;
    private Long likeCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
