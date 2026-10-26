package com.courseshare.category.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.category.dto.CategorySaveRequest;
import com.courseshare.category.dto.CategoryStatusRequest;
import com.courseshare.category.entity.CmsCategory;
import com.courseshare.category.service.CategoryService;
import com.courseshare.category.vo.CategoryTreeNode;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.log.annotation.OperationLog;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RequirePermission("category:manage")
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeNode>> tree() {
        return ApiResponse.success(categoryService.adminTree());
    }

    @RequirePermission("category:manage")
    @OperationLog(module = "分类", action = "新增")
    @PostMapping
    public ResponseEntity<ApiResponse<CmsCategory>> create(@Valid @RequestBody CategorySaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(categoryService.create(request)));
    }

    @RequirePermission("category:manage")
    @OperationLog(module = "分类", action = "编辑")
    @PutMapping("/{id}")
    public ApiResponse<CmsCategory> update(
            @PathVariable Long id,
            @Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @RequirePermission("category:manage")
    @OperationLog(module = "分类", action = "切换状态")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(
            @PathVariable Long id,
            @Valid @RequestBody CategoryStatusRequest request) {
        categoryService.changeStatus(id, request.status(), request.version());
        return ApiResponse.success();
    }

    @RequirePermission("category:manage")
    @OperationLog(module = "分类", action = "删除")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

