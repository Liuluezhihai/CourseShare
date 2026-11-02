package com.courseshare.resource.dto;

import com.courseshare.common.util.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQuery extends PageQuery {
    private String keyword;
    private Long categoryId;
    private Long authorId;
    private Integer status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
    private String sortBy = "publishedAt";
    private String sortDirection = "desc";
}

