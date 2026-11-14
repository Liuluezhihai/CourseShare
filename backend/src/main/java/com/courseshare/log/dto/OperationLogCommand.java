package com.courseshare.log.dto;

public record OperationLogCommand(
        String requestId,
        Long userId,
        String usernameSnapshot,
        String module,
        String action,
        String httpMethod,
        String uri,
        String ip,
        int durationMs,
        boolean success,
        String errorCode,
        String errorMessage,
        String paramsJson
) {
}

