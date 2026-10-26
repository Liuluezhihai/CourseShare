package com.courseshare.user.dto;

import com.courseshare.common.util.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageQuery extends PageQuery {
    private String keyword;
    private Integer status;
}

