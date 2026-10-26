package com.courseshare.category.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeNode {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private Integer status;
    private String description;
    private Integer version;
    private Long resourceCount;
    private List<CategoryTreeNode> children = new ArrayList<>();
}

