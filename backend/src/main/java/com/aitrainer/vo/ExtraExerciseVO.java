package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 额外运动消耗 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "额外运动消耗记录")
public class ExtraExerciseVO {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "运动名称")
    private String exerciseName;

    @Schema(description = "运动描述（如配速、距离等）")
    private String description;

    @Schema(description = "消耗热量 (kcal)")
    private Integer caloriesBurned;

    @Schema(description = "运动时长 (分钟)")
    private Integer durationMinutes;

    @Schema(description = "运动日期")
    private String exerciseDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
