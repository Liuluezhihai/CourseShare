package com.courseshare.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SUCCESS(HttpStatus.OK, "SUCCESS", "成功"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "请求参数不合法"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "请先登录"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "没有执行该操作的权限"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "请求的资源不存在"),
    STATE_CONFLICT(HttpStatus.CONFLICT, "STATE_CONFLICT", "当前状态不允许执行该操作"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "VERSION_CONFLICT", "数据已被其他操作修改，请刷新后重试"),
    DATA_CONFLICT(HttpStatus.CONFLICT, "DATA_CONFLICT", "数据约束冲突"),
    FILE_REJECTED(HttpStatus.BAD_REQUEST, "FILE_REJECTED", "附件不符合上传要求"),
    REQUEST_ERROR(HttpStatus.BAD_REQUEST, "REQUEST_ERROR", "请求格式不正确"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "系统繁忙，请稍后重试");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

