package com.courseshare.interaction.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.interaction.dto.ReviewVisibilityRequest;
import com.courseshare.interaction.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RequirePermission("review:moderate")
    @PatchMapping("/{id}/visibility")
    public ApiResponse<Void> visibility(
            @PathVariable Long id,
            @Valid @RequestBody ReviewVisibilityRequest body) {
        reviewService.setVisibility(id, body.visible());
        return ApiResponse.success();
    }
}
