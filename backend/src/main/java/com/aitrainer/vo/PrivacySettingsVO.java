package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户隐私设置视图对象")
public class PrivacySettingsVO {
    @Schema(description = "是否公开 AI 战报")
    private Boolean publicAiReport;
}
