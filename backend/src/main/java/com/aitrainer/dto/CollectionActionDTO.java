package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "收藏操作参数")
public record CollectionActionDTO(
        @Schema(description = "推文ID")
        @NotNull(message = "推文ID不能为空")
        Long postId,

        @Schema(description = "收藏夹ID")
        @NotNull(message = "请选择收藏夹")
        Long folderId
) {}