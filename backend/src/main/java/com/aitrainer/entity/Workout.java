package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workouts")
public class Workout {
    @TableId(type = IdType.INPUT) // 你的 ID 看起来是 squat, lunge 这种字符串
    private String id;
    private String name;
    private String enName;
    private Integer difficulty;
    private String tags; // 存储为 JSON 字符串
    private String description;
    private String themeColor;
    private String coverUrl; // 如果表里没这个字段，记得在 SQL 里加上或在 Entity 加 @TableField(exist = false)

    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted; // 这里的属性名必须对应 yml 里的 logic-delete-field
}
