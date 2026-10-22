package com.courseshare.common.api;

import org.slf4j.MDC;

public record ApiResponse<T>(String code, String message, T data, String requestId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "成功", data, currentRequestId());
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS", "成功", null, currentRequestId());
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(code, message, null, currentRequestId());
    }

    private static String currentRequestId() {
        String requestId = MDC.get("requestId");
        return requestId == null ? "" : requestId;
    }
}

