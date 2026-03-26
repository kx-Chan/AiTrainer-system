package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "创建推文参数")
public record CreatePostDTO(
        @Schema(description = "推文内容")
        @NotBlank(message = "推文内容不能为空")
        String content,

        @Schema(description = "话题（不带 #）", example = "深蹲打卡挑战赛")
        String topic,

        @Schema(description = "发布设备", example = "Web 端")
        String device,

        @Schema(description = "帖子图片对象 Key 列表")
        List<String> imageKeys
) {
}
