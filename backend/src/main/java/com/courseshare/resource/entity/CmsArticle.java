package com.courseshare.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.courseshare.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_article")
public class CmsArticle extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private Long authorId;
    private String attachmentName;
    private String attachmentPath;
    private String attachmentExt;
    private String attachmentMime;
    private Long attachmentSize;
    private Integer status;
    private Long auditBy;
    private LocalDateTime auditAt;
    private String auditRemark;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Long downloadCount;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
}

