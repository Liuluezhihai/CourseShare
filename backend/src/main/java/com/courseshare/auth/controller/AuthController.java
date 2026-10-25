package com.courseshare.auth.controller;

import com.courseshare.auth.annotation.RequireAuth;
import com.courseshare.auth.dto.LoginRequest;
import com.courseshare.auth.dto.RegisterRequest;
import com.courseshare.auth.service.AuthService;
import com.courseshare.auth.vo.CurrentUserVO;
import com.courseshare.common.api.ApiResponse;
import com.courseshare.log.annotation.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @OperationLog(module = "认证", action = "注册")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @OperationLog(module = "认证", action = "登录")
    @PostMapping("/login")
    public ApiResponse<CurrentUserVO> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.login(request, servletRequest));
    }

    @RequireAuth
    @OperationLog(module = "认证", action = "退出")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @RequireAuth
    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> me(HttpServletRequest request) {
        return ApiResponse.success(authService.current(request));
    }

    @RequireAuth
    @OperationLog(module = "认证", action = "开通创作功能")
    @PostMapping("/author/enable")
    public ApiResponse<CurrentUserVO> enableAuthor(HttpServletRequest request) {
        return ApiResponse.success(authService.enableAuthor(request));
    }
}

