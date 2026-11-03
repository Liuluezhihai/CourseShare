package com.courseshare.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.dto.ReviewSaveRequest;
import com.courseshare.interaction.entity.ResourceReview;
import com.courseshare.interaction.mapper.ResourceReviewMapper;
import com.courseshare.interaction.mapper.ReviewLikeMapper;
import com.courseshare.interaction.vo.MyReviewVO;
import com.courseshare.interaction.vo.ReviewFeedVO;
import com.courseshare.interaction.vo.ReviewStatsVO;
import com.courseshare.interaction.vo.ReviewVO;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.vo.ResourceDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ResourceReviewMapper reviewMapper;
    private final ReviewLikeMapper likeMapper;
    private final CmsArticleMapper articleMapper;

    public ReviewService(
            ResourceReviewMapper reviewMapper,
            ReviewLikeMapper likeMapper,
            CmsArticleMapper articleMapper) {
        this.reviewMapper = reviewMapper;
        this.likeMapper = likeMapper;
        this.articleMapper = articleMapper;
    }

    public ReviewFeedVO feed(Long resourceId, Long viewerId, boolean includeHidden, PageQuery query) {
        requirePublicResource(resourceId);
        Page<ReviewVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        PageResult<ReviewVO> reviews = PageResult.from(
                reviewMapper.selectPublicPage(page, resourceId, viewerId, includeHidden)
        );
        ReviewStatsVO stats = reviewMapper.selectStats(resourceId);
        BigDecimal average = stats == null || stats.getAverageScore() == null
                ? BigDecimal.ZERO.setScale(1)
                : stats.getAverageScore();
        long ratingCount = stats == null || stats.getRatingCount() == null ? 0L : stats.getRatingCount();
        ReviewVO mine = viewerId == null ? null : reviewMapper.selectMine(resourceId, viewerId);
        return new ReviewFeedVO(reviews, average, ratingCount, mine);
    }

    @Transactional
    public ReviewVO save(Long resourceId, Long userId, ReviewSaveRequest request) {
        ResourceDetailVO resource = requirePublicResource(resourceId);
        if (resource.getAuthorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能评价自己发布的资源");
        }
        String content = normalizeContent(request.content());
        ResourceReview existing = reviewMapper.selectByResourceAndUser(resourceId, userId);
        if (existing == null) {
            ResourceReview review = new ResourceReview();
            review.setResourceId(resourceId);
            review.setUserId(userId);
            review.setScore(request.score());
            review.setContent(content);
            review.setStatus(1);
            review.setLikeCount(0L);
            reviewMapper.insert(review);
        } else {
            reviewMapper.updateContent(existing.getId(), request.score(), content, LocalDateTime.now());
        }
        return reviewMapper.selectMine(resourceId, userId);
    }

    @Transactional
    public void delete(Long resourceId, Long userId) {
        reviewMapper.deleteByResourceAndUser(resourceId, userId);
    }

    @Transactional
    public void like(Long reviewId, Long userId) {
        ResourceReview review = requireReview(reviewId);
        if (Integer.valueOf(0).equals(review.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在或已隐藏");
        }
        requirePublicResource(review.getResourceId());
        if (likeMapper.insertIgnore(reviewId, userId) > 0) {
            reviewMapper.incrementLikeCount(reviewId);
        }
    }

    @Transactional
    public void unlike(Long reviewId, Long userId) {
        if (likeMapper.delete(reviewId, userId) > 0) {
            reviewMapper.decrementLikeCount(reviewId);
        }
    }

    @Transactional
    public void setVisibility(Long reviewId, boolean visible) {
        requireReview(reviewId);
        reviewMapper.updateVisibility(reviewId, visible ? 1 : 0);
    }

    public PageResult<MyReviewVO> myPage(Long userId, PageQuery query) {
        Page<MyReviewVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        return PageResult.from(reviewMapper.selectMyPage(page, userId));
    }

    private ResourceReview requireReview(Long reviewId) {
        ResourceReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        return review;
    }

    private ResourceDetailVO requirePublicResource(Long resourceId) {
        ResourceDetailVO resource = articleMapper.selectResourceDetail(resourceId, true);
        if (resource == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在或尚未发布");
        }
        return resource;
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String value = content.trim();
        if (value.length() > 500) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "短评不能超过 500 个字符");
        }
        return value;
    }
}
