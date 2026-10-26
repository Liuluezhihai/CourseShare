package com.courseshare.user.controller;

import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.log.annotation.OperationLog;
import com.courseshare.user.dto.RoleAssignRequest;
import com.courseshare.user.dto.UserPageQuery;
import com.courseshare.user.dto.UserStatusRequest;
import com.courseshare.user.service.UserAdminService;
import com.courseshare.user.vo.RoleVO;
import com.courseshare.user.vo.UserListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final UserAdminService userAdminService;
    private final CurrentUserService currentUserService;

    public AdminUserController(UserAdminService userAdminService, CurrentUserService currentUserService) {
        this.userAdminService = userAdminService;
        this.currentUserService = currentUserService;
    }

    @RequirePermission("user:manage")
    @GetMapping("/users")
    public ApiResponse<PageResult<UserListItemVO>> users(@ModelAttribute UserPageQuery query) {
        return ApiResponse.success(userAdminService.page(query));
    }

    @RequirePermission("user:manage")
    @GetMapping("/roles")
    public ApiResponse<List<RoleVO>> roles() {
        return ApiResponse.success(userAdminService.roles());
    }

    @RequirePermission("user:manage")
    @OperationLog(module = "用户", action = "分配角色")
    @PutMapping("/users/{id}/roles")
    public ApiResponse<UserListItemVO> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignRequest request,
            HttpServletRequest servletRequest) {
        CurrentUser actor = currentUserService.require(servletRequest);
        return ApiResponse.success(userAdminService.assignRoles(id, request, actor.id()));
    }

    @RequirePermission("user:manage")
    @OperationLog(module = "用户", action = "切换状态")
    @PatchMapping("/users/{id}/status")
    public ApiResponse<Void> status(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            HttpServletRequest servletRequest) {
        CurrentUser actor = currentUserService.require(servletRequest);
        userAdminService.changeStatus(id, request.status(), actor.id());
        return ApiResponse.success();
    }
}

