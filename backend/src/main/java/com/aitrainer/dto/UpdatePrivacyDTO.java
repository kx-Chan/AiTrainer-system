package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "更新隐私设置请求")
public record UpdatePrivacyDTO(
        @NotNull(message = "设置项不能为空")
        @Schema(description = "是否公开 AI 战报 (1:公开, 0:私密)")
        Integer publicAiReport
) {}
