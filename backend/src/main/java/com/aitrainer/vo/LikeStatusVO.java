package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点赞状态返回")
public class LikeStatusVO {
    @Schema(description = "是否已点赞")
    private Boolean liked;
    @Schema(description = "点赞总数")
    private Integer likes;
}
