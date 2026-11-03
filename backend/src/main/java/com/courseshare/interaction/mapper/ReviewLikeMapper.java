package com.courseshare.interaction.mapper;

import org.apache.ibatis.annotations.Param;

public interface ReviewLikeMapper {

    int insertIgnore(@Param("reviewId") Long reviewId, @Param("userId") Long userId);

    int delete(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}
