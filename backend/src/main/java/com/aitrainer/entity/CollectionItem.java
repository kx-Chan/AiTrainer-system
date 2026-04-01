package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_item")
public class CollectionItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long folderId;
    private Long postId;
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}