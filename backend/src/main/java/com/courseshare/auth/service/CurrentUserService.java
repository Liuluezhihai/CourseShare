package com.courseshare.auth.service;

import com.courseshare.auth.model.CurrentUser;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.user.entity.SysRole;
import com.courseshare.user.entity.SysUser;
import com.courseshare.user.mapper.SysUserMapper;
import com.courseshare.user.mapper.UserAuthorizationMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrentUserService {

    public static final String SESSION_USER_ID = "AUTH_USER_ID";
    private final SysUserMapper userMapper;
    private final UserAuthorizationMapper authorizationMapper;

    public CurrentUserService(SysUserMapper userMapper, UserAuthorizationMapper authorizationMapper) {
        this.userMapper = userMapper;
        this.authorizationMapper = authorizationMapper;
    }

    public CurrentUser loadOptional(HttpServletRequest request) {
        Object cached = request.getAttribute("currentUser");
        if (cached instanceof CurrentUser currentUser) {
            return currentUser;
        }
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(SESSION_USER_ID) instanceof Long userId)) {
            return null;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(0).equals(user.getStatus()) || Integer.valueOf(1).equals(user.getDeleted())) {
            session.invalidate();
            return null;
        }
        Set<String> roles = authorizationMapper.selectActiveRolesByUserId(userId).stream()
                .map(SysRole::getCode)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> permissions = Set.copyOf(authorizationMapper.selectPermissionCodesByUserId(userId));
        CurrentUser currentUser = new CurrentUser(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                roles,
                permissions
        );
        request.setAttribute("currentUser", currentUser);
        return currentUser;
    }

    public CurrentUser require(HttpServletRequest request) {
        CurrentUser user = loadOptional(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}

