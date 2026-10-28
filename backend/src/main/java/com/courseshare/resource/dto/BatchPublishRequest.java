package com.courseshare.resource.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchPublishRequest(
        @NotEmpty(message = "请选择要发布的资源")
        @Size(max = 100, message = "单次最多发布 100 条资源")
        List<@Valid AuditVersionItem> items
) {
}

