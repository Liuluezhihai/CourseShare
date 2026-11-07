package com.courseshare.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.courseshare.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_resource_review")
public class ResourceReview extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long userId;
    private Integer score;
    private String content;
    private Integer status;
    private Long likeCount;
}
