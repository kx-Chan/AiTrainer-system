package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_guestbook")
public class Guestbook {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;
    private Long toUserId;
    private String content;
    private LocalDateTime createTime;

    private String replyContent;
    private LocalDateTime replyTime;

    @TableLogic // ✅ 标注逻辑删除字段
    private Integer isDeleted;

    private LocalDateTime updateTime;
}
