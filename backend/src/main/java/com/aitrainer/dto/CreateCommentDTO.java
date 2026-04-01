package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "发表评论参数")
public record CreateCommentDTO(
        @Schema(description = "评论内容")
        @NotBlank(message = "评论内容不能为空")
        String content,
        @Schema(description = "父评论ID（可选）")
        Long parentId
) {
}
