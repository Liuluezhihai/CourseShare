package com.courseshare.interaction.controller;

import com.courseshare.auth.annotation.RequireAuth;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.dto.ReviewSaveRequest;
import com.courseshare.interaction.service.ReviewService;
import com.courseshare.interaction.vo.MyReviewVO;
import com.courseshare.interaction.vo.ReviewFeedVO;
import com.courseshare.interaction.vo.ReviewVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    public ReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/resources/{id}/reviews")
    public ApiResponse<ReviewFeedVO> feed(
            @PathVariable Long id,
            @ModelAttribute PageQuery query,
            HttpServletRequest request) {
        CurrentUser current = currentUserService.loadOptional(request);
        boolean includeHidden = current != null && current.hasPermission("review:moderate");
        return ApiResponse.success(reviewService.feed(
                id,
                current == null ? null : current.id(),
                includeHidden,
                query
        ));
    }

    @RequireAuth
    @PutMapping("/resources/{id}/review")
    public ApiResponse<ReviewVO> save(
            @PathVariable Long id,
            @Valid @RequestBody ReviewSaveRequest body,
            HttpServletRequest request) {
        return ApiResponse.success(reviewService.save(id, currentUserService.require(request).id(), body));
    }

    @RequireAuth
    @DeleteMapping("/resources/{id}/review")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        reviewService.delete(id, currentUserService.require(request).id());
        return ApiResponse.success();
    }

    @RequireAuth
    @PutMapping("/reviews/{id}/like")
    public ApiResponse<Void> like(@PathVariable Long id, HttpServletRequest request) {
        reviewService.like(id, currentUserService.require(request).id());
        return ApiResponse.success();
    }

    @RequireAuth
    @DeleteMapping("/reviews/{id}/like")
    public ApiResponse<Void> unlike(@PathVariable Long id, HttpServletRequest request) {
        reviewService.unlike(id, currentUserService.require(request).id());
        return ApiResponse.success();
    }

    @RequireAuth
    @GetMapping("/me/reviews")
    public ApiResponse<PageResult<MyReviewVO>> mine(
            @ModelAttribute PageQuery query,
            HttpServletRequest request) {
        return ApiResponse.success(reviewService.myPage(currentUserService.require(request).id(), query));
    }
}
