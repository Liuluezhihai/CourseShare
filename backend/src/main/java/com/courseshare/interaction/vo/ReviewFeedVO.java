package com.courseshare.interaction.vo;

import com.courseshare.common.api.PageResult;

import java.math.BigDecimal;

public record ReviewFeedVO(
        PageResult<ReviewVO> reviews,
        BigDecimal averageScore,
        long ratingCount,
        ReviewVO mine
) {
}
