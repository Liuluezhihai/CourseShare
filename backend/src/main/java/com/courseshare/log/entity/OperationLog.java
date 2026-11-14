package com.courseshare.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestId;
    private Long userId;
    private String usernameSnapshot;
    private String module;
    private String action;
    private String httpMethod;
    private String uri;
    private String ip;
    private Integer durationMs;
    private Integer success;
    private String errorCode;
    private String errorMessage;
    private String paramsJson;
    private LocalDateTime createdAt;
}

