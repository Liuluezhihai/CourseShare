package com.courseshare.resource.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.log.annotation.OperationLog;
import com.courseshare.resource.dto.AuditApproveRequest;
import com.courseshare.resource.dto.ResourceQuery;
import com.courseshare.resource.dto.ResourceSaveRequest;
import com.courseshare.resource.service.ResourceService;
import com.courseshare.resource.vo.ResourceDetailVO;
import com.courseshare.resource.vo.ResourceListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/author/resources")
public class AuthorResourceController {

    private final ResourceService resourceService;
    private final CurrentUserService currentUserService;

    public AuthorResourceController(ResourceService resourceService, CurrentUserService currentUserService) {
        this.resourceService = resourceService;
        this.currentUserService = currentUserService;
    }

    @RequirePermission("resource:create:own")
    @GetMapping
    public ApiResponse<PageResult<ResourceListItemVO>> page(
            @ModelAttribute ResourceQuery query,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        return ApiResponse.success(resourceService.authorPage(query, user.id()));
    }

    @RequirePermission("resource:edit:own")
    @GetMapping("/{id}")
    public ApiResponse<ResourceDetailVO> detail(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        return ApiResponse.success(resourceService.authorDetail(id, user.id()));
    }

    @RequirePermission("resource:create:own")
    @OperationLog(module = "资源", action = "新建")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResourceDetailVO>> create(
            @Valid @RequestPart("data") ResourceSaveRequest data,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resourceService.create(data, file, user.id())));
    }

    @RequirePermission("resource:edit:own")
    @OperationLog(module = "资源", action = "编辑")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResourceDetailVO> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") ResourceSaveRequest data,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        return ApiResponse.success(resourceService.update(id, data, file, user.id()));
    }

    @RequirePermission("resource:delete:own")
    @OperationLog(module = "资源", action = "作者删除")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.authorDelete(id, user.id());
        return ResponseEntity.noContent().build();
    }

    @RequirePermission("resource:submit:own")
    @OperationLog(module = "资源", action = "提交审核")
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(
            @PathVariable Long id,
            @Valid @RequestBody AuditApproveRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.submit(id, user.id(), body.version());
        return ApiResponse.success();
    }

    @RequirePermission("resource:withdraw:own")
    @OperationLog(module = "资源", action = "撤回审核")
    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody AuditApproveRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.withdraw(id, user.id(), body.version());
        return ApiResponse.success();
    }

    @RequirePermission("resource:offline:own")
    @OperationLog(module = "资源", action = "作者下架")
    @PostMapping("/{id}/offline")
    public ApiResponse<Void> offline(
            @PathVariable Long id,
            @Valid @RequestBody AuditApproveRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.authorOffline(id, user.id(), body.version());
        return ApiResponse.success();
    }
}

