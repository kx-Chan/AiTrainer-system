package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_folder")
public class CollectionFolder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;

    // MyBatis-Plus 会自动将 Integer 映射为数据库的 tinyint
    private Integer isPublic;
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic // 标记为逻辑删除字段
    private Integer isDeleted;
}

