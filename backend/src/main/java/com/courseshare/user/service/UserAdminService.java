package com.courseshare.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.user.dto.RoleAssignRequest;
import com.courseshare.user.dto.UserPageQuery;
import com.courseshare.user.dto.UserRoleView;
import com.courseshare.user.entity.SysRole;
import com.courseshare.user.entity.SysUser;
import com.courseshare.user.mapper.SysRoleMapper;
import com.courseshare.user.mapper.SysUserMapper;
import com.courseshare.user.mapper.UserAuthorizationMapper;
import com.courseshare.user.vo.RoleVO;
import com.courseshare.user.vo.UserListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final UserAuthorizationMapper authorizationMapper;

    public UserAdminService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            UserAuthorizationMapper authorizationMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.authorizationMapper = authorizationMapper;
    }

    public PageResult<UserListItemVO> page(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(query.normalizedPage(), query.normalizedSize()),
                wrapper
        );
        List<Long> userIds = page.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<UserRoleView>> rolesByUser = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserRoleView role : authorizationMapper.selectRolesByUserIds(userIds)) {
                rolesByUser.computeIfAbsent(role.getUserId(), key -> new ArrayList<>()).add(role);
            }
        }
        List<UserListItemVO> records = page.getRecords().stream()
                .map(user -> toVO(user, rolesByUser.getOrDefault(user.getId(), List.of())))
                .toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<RoleVO> roles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId))
                .stream()
                .map(role -> new RoleVO(role.getId(), role.getCode(), role.getName(), role.getDescription()))
                .toList();
    }

    @Transactional
    public UserListItemVO assignRoles(Long userId, RoleAssignRequest request, Long actorId) {
        SysUser user = requireUser(userId);
        Set<String> requestedCodes = request.roleCodes().stream()
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<SysRole> roles = roleMapper.selectActiveByCodes(new ArrayList<>(requestedCodes));
        if (roles.size() != requestedCodes.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "存在无效或已停用的角色");
        }
        Set<String> currentCodes = authorizationMapper.selectActiveRolesByUserId(userId).stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());
        if (currentCodes.contains("ADMIN") && !requestedCodes.contains("ADMIN")) {
            if (user.getId().equals(actorId) || authorizationMapper.countActiveUsersByRoleCode("ADMIN") <= 1) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "不能移除最后一个管理员");
            }
        }
        authorizationMapper.deleteUserRoles(userId);
        for (SysRole role : roles) {
            authorizationMapper.insertUserRole(userId, role.getId());
        }
        List<UserRoleView> roleViews = authorizationMapper.selectRolesByUserIds(List.of(userId));
        return toVO(user, roleViews);
    }

    @Transactional
    public void changeStatus(Long userId, int status, Long actorId) {
        SysUser user = requireUser(userId);
        if (user.getId().equals(actorId) && status == 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "不能停用当前登录账号");
        }
        Set<String> roles = authorizationMapper.selectActiveRolesByUserId(userId).stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());
        if (status == 0 && roles.contains("ADMIN") && authorizationMapper.countActiveUsersByRoleCode("ADMIN") <= 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "不能停用最后一个管理员");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserListItemVO toVO(SysUser user, List<UserRoleView> roles) {
        UserListItemVO vo = new UserListItemVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setRoleCodes(roles.stream().map(UserRoleView::getRoleCode).toList());
        vo.setRoleNames(roles.stream().map(UserRoleView::getRoleName).toList());
        return vo;
    }
}

