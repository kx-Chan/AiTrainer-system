package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑收藏夹请求对象")
public record UpdateFolderDTO(
        @Schema(description = "收藏夹名称")
        @NotBlank(message = "名称不能为空")
        @Size(max = 20, message = "名称最多20个字符")
        String name,

        @Schema(description = "是否公开 (0:私密, 1:公开)")
        Integer isPublic
) {}