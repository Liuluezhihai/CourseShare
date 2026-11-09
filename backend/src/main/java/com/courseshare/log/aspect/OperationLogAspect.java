package com.courseshare.log.aspect;

import com.courseshare.auth.model.CurrentUser;
import com.courseshare.auth.service.CurrentUserService;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.common.util.ClientIp;
import com.courseshare.log.annotation.OperationLog;
import com.courseshare.log.dto.OperationLogCommand;
import com.courseshare.log.service.LogParamsSerializer;
import com.courseshare.log.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Order(0)
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final CurrentUserService currentUserService;
    private final OperationLogService operationLogService;
    private final LogParamsSerializer paramsSerializer;

    public OperationLogAspect(
            CurrentUserService currentUserService,
            OperationLogService operationLogService,
            LogParamsSerializer paramsSerializer) {
        this.currentUserService = currentUserService;
        this.operationLogService = operationLogService;
        this.paramsSerializer = paramsSerializer;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long started = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            record(joinPoint, operationLog, started, null);
            return result;
        } catch (Throwable throwable) {
            record(joinPoint, operationLog, started, throwable);
            throw throwable;
        }
    }

    private void record(ProceedingJoinPoint joinPoint, OperationLog annotation, long started, Throwable throwable) {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }
        try {
            CurrentUser user = currentUserService.loadOptional(request);
            ErrorCode errorCode = throwable instanceof BusinessException businessException
                    ? businessException.getErrorCode()
                    : throwable == null ? null : ErrorCode.INTERNAL_ERROR;
            String message = throwable == null ? null : throwable.getMessage();
            OperationLogCommand command = new OperationLogCommand(
                    String.valueOf(request.getAttribute("requestId")),
                    user == null ? null : user.id(),
                    user == null ? null : user.username(),
                    annotation.module(),
                    annotation.action(),
                    request.getMethod(),
                    request.getRequestURI(),
                    ClientIp.resolve(request),
                    (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started),
                    throwable == null,
                    errorCode == null ? null : errorCode.getCode(),
                    message,
                    paramsSerializer.serialize(joinPoint.getArgs())
            );
            operationLogService.record(command);
        } catch (Exception exception) {
            log.error("操作日志落库失败", exception);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}

