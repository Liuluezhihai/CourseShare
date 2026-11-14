package com.courseshare.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.log.dto.LogQuery;
import com.courseshare.log.dto.OperationLogCommand;
import com.courseshare.log.entity.OperationLog;
import com.courseshare.log.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OperationLogService {

    private final OperationLogMapper logMapper;

    public OperationLogService(OperationLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(OperationLogCommand command) {
        OperationLog log = new OperationLog();
        log.setRequestId(command.requestId());
        log.setUserId(command.userId());
        log.setUsernameSnapshot(command.usernameSnapshot());
        log.setModule(limit(command.module(), 50));
        log.setAction(limit(command.action(), 50));
        log.setHttpMethod(limit(command.httpMethod(), 10));
        log.setUri(limit(command.uri(), 255));
        log.setIp(limit(command.ip(), 45));
        log.setDurationMs(command.durationMs());
        log.setSuccess(command.success() ? 1 : 0);
        log.setErrorCode(limit(command.errorCode(), 50));
        log.setErrorMessage(limit(command.errorMessage(), 500));
        log.setParamsJson(command.paramsJson());
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    public PageResult<OperationLog> page(LogQuery query) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition
                    .like(OperationLog::getRequestId, keyword)
                    .or()
                    .like(OperationLog::getUsernameSnapshot, keyword)
                    .or()
                    .like(OperationLog::getUri, keyword));
        }
        if (query.getModule() != null && !query.getModule().isBlank()) {
            wrapper.eq(OperationLog::getModule, query.getModule().trim());
        }
        if (query.getUserId() != null) {
            wrapper.eq(OperationLog::getUserId, query.getUserId());
        }
        if (query.getSuccess() != null) {
            wrapper.eq(OperationLog::getSuccess, query.getSuccess());
        }
        if (query.getDateFrom() != null) {
            wrapper.ge(OperationLog::getCreatedAt, query.getDateFrom().atStartOfDay());
        }
        if (query.getDateTo() != null) {
            wrapper.lt(OperationLog::getCreatedAt, query.getDateTo().plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return PageResult.from(logMapper.selectPage(
                new Page<>(query.normalizedPage(), query.normalizedSize()),
                wrapper
        ));
    }

    private String limit(String value, int length) {
        if (value == null) {
            return null;
        }
        return value.length() <= length ? value : value.substring(0, length);
    }
}

