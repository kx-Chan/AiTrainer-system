package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_follows")
public class UserFollows {
    private Long id; // 自增主键
    private Long followerId; // 关注者ID
    private Long followedId; // 被关注者ID
    private LocalDateTime createdAt; // 创建时间
}
