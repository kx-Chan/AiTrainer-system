package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 首次引导页提交用户资料的 DTO。
 */
@Schema(description = "首次引导页用户资料")
public record OnboardingProfileDTO(

        @Schema(description = "昵称", example = "健身小萌新")
        @NotBlank(message = "昵称不能为空")
        @Size(max = 20, message = "昵称不能超过 20 个字符")
        String nickname,

        @Schema(description = "性别", example = "male")
        @NotBlank(message = "性别不能为空")
        String gender,

        @Schema(description = "健身目标", example = "减脂")
        @NotBlank(message = "必须选择一个健身目标")
        String goal,

        @Schema(description = "身高 (cm)", example = "175")
        @Min(value = 100, message = "身高必须在 100 到 250 cm 之间")
        @Max(value = 250, message = "身高必须在 100 到 250 cm 之间")
        Integer height,

        @Schema(description = "体重 (kg)", example = "70.5")
        @DecimalMin(value = "30.0", message = "体重必须在 30 到 200 kg 之间")
        @DecimalMax(value = "200.0", message = "体重必须在 30 到 200 kg 之间")
        BigDecimal weight,

        @Schema(description = "体脂率 (%)", example = "22.5")
        @DecimalMin(value = "1.0", message = "体脂率必须在 1 到 50 % 之间")
        @DecimalMax(value = "50.0", message = "体脂率必须在 1 到 50 % 之间")
        BigDecimal bodyFat
) {
}
