package com.courseshare.log.dto;

import com.courseshare.common.util.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogQuery extends PageQuery {
    private String keyword;
    private String module;
    private Long userId;
    private Integer success;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
}

