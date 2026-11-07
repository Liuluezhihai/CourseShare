package com.courseshare.resource.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.resource.dto.ResourceQuery;
import com.courseshare.resource.service.FileService;
import com.courseshare.resource.service.ResourceService;
import com.courseshare.resource.vo.ResourceDetailVO;
import com.courseshare.resource.vo.ResourceListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/resources")
public class PublicResourceController {

    private final ResourceService resourceService;
    private final CurrentUserService currentUserService;

    public PublicResourceController(ResourceService resourceService, CurrentUserService currentUserService) {
        this.resourceService = resourceService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<PageResult<ResourceListItemVO>> page(@ModelAttribute ResourceQuery query) {
        return ApiResponse.success(resourceService.publicPage(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<ResourceDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(resourceService.publicDetail(id));
    }

    @RequirePermission("resource:download")
    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable Long id,
            HttpServletRequest request) {
        currentUserService.require(request);
        FileService.DownloadFile download = resourceService.prepareDownload(id);
        String filename = download.originalName() == null || download.originalName().isBlank()
                ? "resource"
                : download.originalName();
        StreamingResponseBody body = outputStream -> {
            try (InputStream input = Files.newInputStream(download.path())) {
                input.transferTo(outputStream);
            }
            resourceService.recordDownload(id);
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
}

