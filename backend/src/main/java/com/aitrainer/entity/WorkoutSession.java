package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 训练战报实体类，对应 workout_sessions 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("workout_sessions")
public class WorkoutSession {

    /**
     * 战报记录 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID。
     */
    private Long userId;

    /**
     * 训练项目 ID（如：squat, pushup）。
     */
    private String workoutId;

    /**
     * 综合评分 (0-100)。
     */
    private Integer score;

    /**
     * 评级 (S/A/B/C)。
     */
    private String grade;

    /**
     * AI 点评文本。
     */
    private String comment;

    /**
     * 有效动作次数。
     */
    private Integer validReps;

    /**
     * 异常（无效）动作次数。
     */
    private Integer invalidReps;

    /**
     * 训练总时长（分）。
     */
    private Integer durationMinutes;

    /**
     * 消耗热量 (kcal)。
     */
    private Integer caloriesBurned;

    /**
     * 战报点赞总数。
     */
    private Integer likeCount;

    /**
     * 五维雷达图评分。
     * 计科建议：数据库存 JSON 字符串，Java 映射为 String。
     */
    private String radarScores;

    /**
     * 纠错抓拍图片 ObjectKey 列表。
     * 计科建议：数据库存 JSON 数组字符串，如 ["key1.jpg", "key2.jpg"]。
     */
    private String snapshots;

    /**
     * 创建时间（训练完成时间）。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标识 (0:未删除, 1:已删除)。
     */
    @TableLogic
    private Integer isDeleted;
}
