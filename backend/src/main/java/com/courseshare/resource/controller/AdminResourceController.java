package com.courseshare.resource.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.log.annotation.OperationLog;
import com.courseshare.resource.dto.AuditApproveRequest;
import com.courseshare.resource.dto.AuditRejectRequest;
import com.courseshare.resource.dto.BatchPublishRequest;
import com.courseshare.resource.dto.ResourceQuery;
import com.courseshare.resource.service.FileService;
import com.courseshare.resource.service.ResourceService;
import com.courseshare.resource.vo.ResourceListItemVO;
import com.courseshare.resource.vo.ResourceDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceController {

    private final ResourceService resourceService;
    private final CurrentUserService currentUserService;

    public AdminResourceController(ResourceService resourceService, CurrentUserService currentUserService) {
        this.resourceService = resourceService;
        this.currentUserService = currentUserService;
    }

    @RequirePermission("resource:audit:list")
    @GetMapping
    public ApiResponse<PageResult<ResourceListItemVO>> page(@ModelAttribute ResourceQuery query) {
        return ApiResponse.success(resourceService.adminPage(query));
    }

    @RequirePermission("resource:audit:list")
    @GetMapping("/{id}")
    public ApiResponse<ResourceDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(resourceService.adminDetail(id));
    }

    @RequirePermission("resource:audit:list")
    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable Long id) {
        FileService.DownloadFile download = resourceService.prepareAdminDownload(id);
        String filename = download.originalName() == null || download.originalName().isBlank()
                ? "resource"
                : download.originalName();
        StreamingResponseBody body = outputStream -> {
            try (InputStream input = Files.newInputStream(download.path())) {
                input.transferTo(outputStream);
            }
        };

        long contentLength;
        try {
            contentLength = Files.size(download.path());
        } catch (IOException exception) {
            contentLength = 0;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    @RequirePermission("resource:audit")
    @OperationLog(module = "审核", action = "审核通过")
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(
            @PathVariable Long id,
            @Valid @RequestBody AuditApproveRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.approve(id, body.version(), user.id());
        return ApiResponse.success();
    }

    @RequirePermission("resource:audit")
    @OperationLog(module = "审核", action = "驳回资源")
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(
            @PathVariable Long id,
            @Valid @RequestBody AuditRejectRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        resourceService.reject(id, body.version(), body.remark(), user.id());
        return ApiResponse.success();
    }

    @RequirePermission("resource:publish")
    @OperationLog(module = "审核", action = "批量发布")
    @PostMapping("/batch-publish")
    public ApiResponse<Integer> batchPublish(
            @Valid @RequestBody BatchPublishRequest body,
            HttpServletRequest request) {
        CurrentUser user = currentUserService.require(request);
        return ApiResponse.success(resourceService.batchPublish(body, user.id()));
    }

    @RequirePermission("resource:offline:any")
    @OperationLog(module = "资源", action = "管理员下架")
    @PostMapping("/{id}/offline")
    public ApiResponse<Void> offline(
            @PathVariable Long id,
            @Valid @RequestBody AuditApproveRequest body) {
        resourceService.adminOffline(id, body.version());
        return ApiResponse.success();
    }

    @RequirePermission("resource:delete:any")
    @OperationLog(module = "资源", action = "管理员删除")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}

