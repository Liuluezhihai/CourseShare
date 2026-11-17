package com.courseshare.auth.service;

import com.courseshare.auth.dto.RegisterRequest;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.vo.CurrentUserVO;
import com.courseshare.user.entity.SysRole;
import com.courseshare.user.entity.SysUser;
import com.courseshare.user.mapper.SysRoleMapper;
import com.courseshare.user.mapper.SysUserMapper;
import com.courseshare.user.mapper.UserAuthorizationMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private UserAuthorizationMapper authorizationMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    @Test
    void registrationCanEnableAuthorWithoutAdministrator() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(42L);
            return 1;
        });
        SysRole userRole = role(1L, "USER");
        SysRole authorRole = role(2L, "AUTHOR");
        when(roleMapper.selectActiveByCodes(any())).thenReturn(List.of(userRole, authorRole));

        authService.register(new RegisterRequest(
                "learner01",
                "password123",
                "学习者",
                null,
                true
        ));

        verify(authorizationMapper).insertUserRole(42L, 1L);
        verify(authorizationMapper).insertUserRole(42L, 2L);
    }

    @Test
    void authenticatedUserCanEnableAuthorLater() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        CurrentUser before = new CurrentUser(
                42L,
                "learner01",
                "学习者",
                null,
                Set.of("USER"),
                Set.of("resource:download")
        );
        CurrentUser after = new CurrentUser(
                42L,
                "learner01",
                "学习者",
                null,
                Set.of("USER", "AUTHOR"),
                Set.of("resource:download", "resource:create:own")
        );
        when(currentUserService.require(request)).thenReturn(before, after);
        when(roleMapper.selectActiveByCodes(List.of("AUTHOR")))
                .thenReturn(List.of(role(2L, "AUTHOR")));

        CurrentUserVO result = authService.enableAuthor(request);

        verify(authorizationMapper).insertUserRoleIfAbsent(42L, 2L);
        verify(request).removeAttribute("currentUser");
        assertTrue(result.roles().contains("AUTHOR"));
        assertTrue(result.permissions().contains("resource:create:own"));
    }

    private SysRole role(Long id, String code) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setCode(code);
        return role;
    }
}
