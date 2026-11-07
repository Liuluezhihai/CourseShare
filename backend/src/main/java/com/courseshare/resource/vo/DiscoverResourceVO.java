package com.courseshare.resource.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscoverResourceVO extends ResourceListItemVO {
    private BigDecimal averageRating;
    private Long ratingCount;
    private Long favoriteCount;
    private Long reviewLikeCount;
    private Long popularScore;
}
