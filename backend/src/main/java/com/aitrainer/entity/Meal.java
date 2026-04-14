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
 * 饮食记录实体类，对应 meals 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("meals")
public class Meal {

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
     * 进餐时间。
     */
    private LocalDateTime mealTime;

    /**
     * 餐次类型（breakfast/lunch/dinner/snack）。
     */
    private String mealType;

    /**
     * 食物名称。
     */
    private String foodName;

    /**
     * 热量 (kcal)。
     */
    private Integer calories;

    /**
     * 食物重量 (g)。
     */
    private Integer weight;

    /**
     * 标签样式。
     */
    private String tagType;

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
