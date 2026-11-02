package com.courseshare.resource.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceListItemVO {
    private Long id;
    private String title;
    private String summary;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String attachmentName;
    private String attachmentExt;
    private Long attachmentSize;
    private Integer status;
    private Long viewCount;
    private Long downloadCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}

