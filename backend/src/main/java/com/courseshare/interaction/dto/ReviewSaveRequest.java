package com.courseshare.interaction.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewSaveRequest(
        @NotNull(message = "请选择评分")
        @Min(value = 1, message = "评分最低为 1 星")
        @Max(value = 5, message = "评分最高为 5 星")
        Integer score,
        @Size(max = 500, message = "短评不能超过 500 个字符")
        String content
) {
}
