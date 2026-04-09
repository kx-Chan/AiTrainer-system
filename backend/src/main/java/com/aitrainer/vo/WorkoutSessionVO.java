package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 训练战报视图对象")
public class WorkoutSessionVO {

    @Schema(description = "战报记录 ID")
    private Long id;

    @Schema(description = "项目 ID (如: squat, pushup)")
    private String workoutId;

    @Schema(description = "综合评分 (0-100)")
    private Integer score;

    @Schema(description = "评级 (S/A/B/C)")
    private String grade;

    @Schema(description = "评级对应的前端 CSS 类名")
    private String gradeLevel;

    @Schema(description = "AI 点评文本")
    private String comment;

    @Schema(description = "有效动作次数")
    private Integer validReps;

    @Schema(description = "异常动作次数")
    private Integer invalidReps;

    @Schema(description = "训练时长 (秒)")
    private Integer durationSeconds;

    @Schema(description = "消耗热量 (kcal)")
    private Integer caloriesBurned;

    @Schema(description = "五维评分数据 (Key 为维度名, Value 为分数)")
    private Map<String, Integer> radarScores;

    @Schema(description = "纠错抓拍图片 URL 列表")
    private List<String> snapshots;

    @Schema(description = "训练完成时间")
    private LocalDateTime createdAt;

    @Schema(description = "点赞总数")
    private Integer likes;

    @Schema(description = "当前访客是否已点赞")
    private Boolean liked;
}
