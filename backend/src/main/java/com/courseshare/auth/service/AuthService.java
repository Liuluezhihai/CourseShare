package com.courseshare.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.courseshare.auth.dto.LoginRequest;
import com.courseshare.auth.dto.RegisterRequest;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.vo.CurrentUserVO;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.user.entity.SysRole;
import com.courseshare.user.entity.SysUser;
import com.courseshare.user.mapper.SysRoleMapper;
import com.courseshare.user.mapper.SysUserMapper;
import com.courseshare.user.mapper.UserAuthorizationMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final UserAuthorizationMapper authorizationMapper;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            UserAuthorizationMapper authorizationMapper,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.authorizationMapper = authorizationMapper;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username().trim()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "用户名已存在");
        }
        if (request.email() != null && !request.email().isBlank()) {
            Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, request.email().trim().toLowerCase()));
            if (emailCount > 0) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT, "邮箱已被使用");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setEmail(request.email() == null || request.email().isBlank()
                ? null
                : request.email().trim().toLowerCase());
        user.setStatus(1);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "用户名或邮箱已存在");
        }

        List<String> roleCodes = Boolean.TRUE.equals(request.enableAuthor())
                ? List.of("USER", "AUTHOR")
                : List.of("USER");
        List<SysRole> roles = roleMapper.selectActiveByCodes(roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "系统默认角色未初始化");
        }
        roles.forEach(role -> authorizationMapper.insertUserRole(user.getId(), role.getId()));
    }

    @Transactional
    public CurrentUserVO enableAuthor(HttpServletRequest servletRequest) {
        CurrentUser current = currentUserService.require(servletRequest);
        if (current.hasRole("AUTHOR") || current.hasPermission("resource:create:own")) {
            return toVO(current);
        }
        SysRole authorRole = roleMapper.selectActiveByCodes(List.of("AUTHOR")).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "创作功能暂不可用，请稍后再试"
                ));
        authorizationMapper.insertUserRoleIfAbsent(current.id(), authorRole.getId());
        servletRequest.removeAttribute("currentUser");
        return toVO(currentUserService.require(servletRequest));
    }

    public CurrentUserVO login(LoginRequest request, HttpServletRequest servletRequest) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username().trim())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已停用");
        }

        HttpSession existing = servletRequest.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(CurrentUserService.SESSION_USER_ID, user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        CurrentUser current = currentUserService.require(servletRequest);
        return toVO(current);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public CurrentUserVO current(HttpServletRequest request) {
        return toVO(currentUserService.require(request));
    }

    private CurrentUserVO toVO(CurrentUser current) {
        return new CurrentUserVO(
                current.id(),
                current.username(),
                current.nickname(),
                current.email(),
                current.roles(),
                current.permissions()
        );
    }
}

