package com.courseshare.interaction.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewVisibilityRequest(@NotNull(message = "请指定评价显示状态") Boolean visible) {
}
