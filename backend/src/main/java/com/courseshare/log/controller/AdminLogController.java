package com.courseshare.log.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.log.dto.LogQuery;
import com.courseshare.log.entity.OperationLog;
import com.courseshare.log.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    private final OperationLogService operationLogService;

    public AdminLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @RequirePermission("log:view")
    @GetMapping
    public ApiResponse<PageResult<OperationLog>> page(@ModelAttribute LogQuery query) {
        return ApiResponse.success(operationLogService.page(query));
    }
}

