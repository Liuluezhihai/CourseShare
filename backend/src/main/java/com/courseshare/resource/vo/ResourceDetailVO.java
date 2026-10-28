package com.courseshare.resource.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceDetailVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String attachmentName;
    private String attachmentExt;
    private String attachmentMime;
    private Long attachmentSize;
    private Integer status;
    private Long auditBy;
    private String auditorName;
    private LocalDateTime auditAt;
    private String auditRemark;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Long downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}

