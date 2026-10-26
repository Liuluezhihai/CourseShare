package com.courseshare.category.controller;

import com.courseshare.category.service.CategoryService;
import com.courseshare.category.vo.CategoryTreeNode;
import com.courseshare.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeNode>> tree() {
        return ApiResponse.success(categoryService.publicTree());
    }
}

