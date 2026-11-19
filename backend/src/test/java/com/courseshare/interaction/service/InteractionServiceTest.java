package com.courseshare.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.dto.ReviewSaveRequest;
import com.courseshare.interaction.entity.ResourceReview;
import com.courseshare.interaction.mapper.ResourceFavoriteMapper;
import com.courseshare.interaction.mapper.ResourceReviewMapper;
import com.courseshare.interaction.mapper.ReviewLikeMapper;
import com.courseshare.interaction.vo.ReviewStatsVO;
import com.courseshare.interaction.vo.ReviewVO;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.vo.DiscoverResourceVO;
import com.courseshare.resource.vo.ResourceDetailVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private CmsArticleMapper articleMapper;
    @Mock
    private ResourceFavoriteMapper favoriteMapper;
    @Mock
    private ResourceReviewMapper reviewMapper;
    @Mock
    private ReviewLikeMapper likeMapper;
    @InjectMocks
    private FavoriteService favoriteService;
    @InjectMocks
    private ReviewService reviewService;
    @InjectMocks
    private DiscoverService discoverService;

    @Test
    void favoriteIsIdempotentAtMapperBoundary() {
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));
        when(favoriteMapper.insertIgnore(9L, 7L)).thenReturn(0);

        favoriteService.favorite(7L, 9L);

        verify(favoriteMapper).insertIgnore(9L, 7L);
    }

    @Test
    void authorCannotReviewOwnResource() {
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.save(7L, 3L, new ReviewSaveRequest(5, "很好"))
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(reviewMapper, never()).insert(any(ResourceReview.class));
    }

    @Test
    void duplicateLikeDoesNotIncrementCounterTwice() {
        ResourceReview review = review(11L, 7L, 3L);
        when(reviewMapper.selectById(11L)).thenReturn(review);
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));
        when(likeMapper.insertIgnore(11L, 9L)).thenReturn(0);

        reviewService.like(11L, 9L);

        verify(reviewMapper, never()).incrementLikeCount(11L);
    }

    @Test
    void updatingExistingReviewKeepsItsCurrentVisibility() {
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));
        when(reviewMapper.selectByResourceAndUser(7L, 9L)).thenReturn(review(11L, 7L, 9L));
        when(reviewMapper.selectMine(7L, 9L)).thenReturn(new ReviewVO());

        reviewService.save(7L, 9L, new ReviewSaveRequest(4, "更新短评"));

        verify(reviewMapper).updateContent(any(), any(), any(), any());
        verify(reviewMapper, never()).updateVisibility(any(), any());
        verify(reviewMapper, never()).insert(any(ResourceReview.class));
    }

    @Test
    void userCanLikeOwnReview() {
        when(reviewMapper.selectById(11L)).thenReturn(review(11L, 7L, 9L));
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));
        when(likeMapper.insertIgnore(11L, 9L)).thenReturn(1);

        reviewService.like(11L, 9L);

        verify(reviewMapper).incrementLikeCount(11L);
    }

    @Test
    void feedReturnsDefaultScoreWhenThereAreNoReviews() {
        when(articleMapper.selectResourceDetail(7L, true)).thenReturn(resource(7L, 3L));
        when(reviewMapper.selectPublicPage(any(), any(), any(), anyBoolean())).thenReturn(new Page<>());
        ReviewStatsVO stats = new ReviewStatsVO();
        stats.setAverageScore(BigDecimal.ZERO);
        stats.setRatingCount(0L);
        when(reviewMapper.selectStats(7L)).thenReturn(stats);

        assertEquals(BigDecimal.ZERO, reviewService.feed(7L, null, false, new PageQuery()).averageScore());
    }

    @Test
    void invalidDiscoverSortFallsBackToPopular() {
        when(articleMapper.selectDiscoverPage(any(), any())).thenReturn(new Page<DiscoverResourceVO>());

        PageResult<DiscoverResourceVO> result = discoverService.page("unknown", new PageQuery());

        assertEquals(List.of(), result.records());
        verify(articleMapper).selectDiscoverPage(any(), org.mockito.ArgumentMatchers.eq("popular"));
    }

    private ResourceDetailVO resource(Long id, Long authorId) {
        ResourceDetailVO resource = new ResourceDetailVO();
        resource.setId(id);
        resource.setAuthorId(authorId);
        return resource;
    }

    private ResourceReview review(Long id, Long resourceId, Long userId) {
        ResourceReview review = new ResourceReview();
        review.setId(id);
        review.setResourceId(resourceId);
        review.setUserId(userId);
        review.setStatus(1);
        return review;
    }
}
