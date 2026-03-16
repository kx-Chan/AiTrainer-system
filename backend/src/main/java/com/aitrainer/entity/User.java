package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 表示用户表的 User 实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /**
     * 用户唯一识别 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 邮箱地址。
     */
    private String email;

    /**
     * 加密后的密码哈希。
     */
    private String passwordHash;

    /**
     * 用户头像 URL。
     */
    private String avatar;

    /**
     * 是否为 PRO 会员。
     */
    private boolean isPro;

    /**
     * 是否首次登录。
     */
    private boolean isFirstLogin;

    /**
     * 注册时间。
     */
    private LocalDateTime createdAt;

    /**
     * 资料更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除标记。
     */
    private LocalDateTime deletedAt;
}
