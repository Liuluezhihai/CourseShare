package com.courseshare.interaction.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewStatsVO {
    private BigDecimal averageScore;
    private Long ratingCount;
}
