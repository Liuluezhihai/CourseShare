package com.courseshare.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.interaction.entity.ResourceReview;
import com.courseshare.interaction.vo.MyReviewVO;
import com.courseshare.interaction.vo.ReviewStatsVO;
import com.courseshare.interaction.vo.ReviewVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ResourceReviewMapper extends BaseMapper<ResourceReview> {

    ResourceReview selectByResourceAndUser(
            @Param("resourceId") Long resourceId,
            @Param("userId") Long userId
    );

    IPage<ReviewVO> selectPublicPage(
            Page<ReviewVO> page,
            @Param("resourceId") Long resourceId,
            @Param("viewerId") Long viewerId,
            @Param("includeHidden") boolean includeHidden
    );

    ReviewVO selectMine(
            @Param("resourceId") Long resourceId,
            @Param("userId") Long userId
    );

    ReviewStatsVO selectStats(@Param("resourceId") Long resourceId);

    IPage<MyReviewVO> selectMyPage(Page<MyReviewVO> page, @Param("userId") Long userId);

    int updateContent(
            @Param("id") Long id,
            @Param("score") Integer score,
            @Param("content") String content,
            @Param("now") LocalDateTime now
    );

    int updateVisibility(@Param("id") Long id, @Param("status") Integer status);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int deleteByResourceAndUser(@Param("resourceId") Long resourceId, @Param("userId") Long userId);
}
