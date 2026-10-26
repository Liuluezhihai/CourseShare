package com.courseshare.auth.aspect;

import com.courseshare.auth.annotation.RequireAuth;
import com.courseshare.auth.annotation.RequirePermission;
import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Order(1)
public class PermissionAspect {

    private final CurrentUserService currentUserService;

    public PermissionAspect(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Around("@annotation(requireAuth)")
    public Object requireAuthentication(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {
        currentUserService.require(currentRequest());
        return joinPoint.proceed();
    }

    @Around("@annotation(requirePermission)")
    public Object requirePermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        CurrentUser user = currentUserService.require(currentRequest());
        if (!user.hasPermission(requirePermission.value())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}

