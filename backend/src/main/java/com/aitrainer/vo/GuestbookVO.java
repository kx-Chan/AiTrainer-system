package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留言板条目视图对象")
public class GuestbookVO {

    @Schema(description = "留言记录ID")
    private Long id;

    @Schema(description = "留言发起者ID")
    private Long fromUserId;

    @Schema(description = "留言发起者昵称")
    private String fromUserName;

    @Schema(description = "留言发起者头像")
    private String fromUserAvatar;

    @Schema(description = "接收留言的用户ID")
    private Long toUserId;

    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "留言时间")
    private LocalDateTime createTime;

    @Schema(description = "主人回复内容")
    private String replyContent;

    @Schema(description = "主人回复时间")
    private LocalDateTime replyTime;
}