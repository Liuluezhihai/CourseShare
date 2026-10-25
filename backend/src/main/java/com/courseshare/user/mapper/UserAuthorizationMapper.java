package com.courseshare.user.mapper;

import com.courseshare.user.dto.UserRoleView;
import com.courseshare.user.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAuthorizationMapper {

    List<SysRole> selectActiveRolesByUserId(@Param("userId") Long userId);

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    List<UserRoleView> selectRolesByUserIds(@Param("userIds") List<Long> userIds);

    int deleteUserRoles(@Param("userId") Long userId);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int insertUserRoleIfAbsent(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int countActiveUsersByRoleCode(@Param("roleCode") String roleCode);
}

