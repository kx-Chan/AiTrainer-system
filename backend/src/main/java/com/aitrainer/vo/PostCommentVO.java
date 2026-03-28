package com.aitrainer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "帖子评论视图对象")
public class PostCommentVO {
    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "评论用户ID")
    private Long userId;
    @Schema(description = "评论用户昵称")
    private String author;
    @Schema(description = "评论用户头像")
    private String avatar;
    @Schema(description = "是否PRO")
    private Boolean isPro;
    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime time;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "父评论ID")
    private Long parentId;
}
