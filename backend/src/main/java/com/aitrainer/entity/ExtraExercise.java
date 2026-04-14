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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户额外运动消耗记录实体类，对应 extra_exercises 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("extra_exercises")
public class ExtraExercise {

    /**
     * 记录 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID。
     */
    private Long userId;

    /**
     * 运动名称。
     */
    private String exerciseName;

    /**
     * 消耗热量 (kcal)。
     */
    private Integer caloriesBurned;

    /**
     * 运动时长 (分钟)。
     */
    private Integer durationMinutes;

    /**
     * 运动日期。
     */
    private LocalDate exerciseDate;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标识 (0:未删除, 1:已删除)。
     */
    @TableLogic
    private Integer isDeleted;
}
