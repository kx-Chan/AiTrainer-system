package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户资料实体类，对应 user_profiles 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_profiles")
public class UserProfile {

    /**
     * 用户 ID，与 users 表主键关联。
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 性别。
     */
    private String gender;

    /**
     * 健身目标（减脂、增肌、保持）。
     */
    private String goal;

    /**
     * 个性签名。
     */
    private String bio;

    /**
     * 身高 (cm)。
     */
    private Integer height;

    /**
     * 体重 (kg)。
     */
    private BigDecimal weight;

    /**
     * 体脂率 (%)。
     */
    private BigDecimal bodyFat;

    /**
     * 记录更新时间。
     */
    private LocalDateTime updatedAt;
}
