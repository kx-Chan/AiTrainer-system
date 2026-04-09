package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_privacy_settings")
public class UserPrivacySettings {
    @TableId(type = IdType.INPUT) // 这里直接用用户ID作为主键
    private Long userId;
    private Integer publicAiReport; // 1:公开, 0:私密
    private LocalDateTime updatedAt;
}
