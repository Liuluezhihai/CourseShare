package com.courseshare.interaction.controller;

import com.courseshare.auth.annotation.RequireAuth;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.service.FavoriteService;
import com.courseshare.resource.vo.ResourceListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CurrentUserService currentUserService;

    public FavoriteController(FavoriteService favoriteService, CurrentUserService currentUserService) {
        this.favoriteService = favoriteService;
        this.currentUserService = currentUserService;
    }

    @RequireAuth
    @PutMapping("/resources/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long id, HttpServletRequest request) {
        favoriteService.favorite(id, currentUserService.require(request).id());
        return ApiResponse.success();
    }

    @RequireAuth
    @DeleteMapping("/resources/{id}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long id, HttpServletRequest request) {
        favoriteService.unfavorite(id, currentUserService.require(request).id());
        return ApiResponse.success();
    }

    @RequireAuth
    @GetMapping("/me/favorites")
    public ApiResponse<PageResult<ResourceListItemVO>> page(
            @ModelAttribute PageQuery query,
            HttpServletRequest request) {
        return ApiResponse.success(favoriteService.page(currentUserService.require(request).id(), query));
    }

    @RequireAuth
    @GetMapping("/me/favorite-ids")
    public ApiResponse<List<Long>> ids(HttpServletRequest request) {
        return ApiResponse.success(favoriteService.resourceIds(currentUserService.require(request).id()));
    }
}
