package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("post_images")
public class PostImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String objectKey;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
