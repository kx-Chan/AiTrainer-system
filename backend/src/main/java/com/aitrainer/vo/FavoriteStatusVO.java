package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏状态返回")
public class FavoriteStatusVO {
    @Schema(description = "是否已收藏")
    private Boolean favorited;
    @Schema(description = "收藏总数")
    private Integer favorites;
    @Schema(description = "该推文所在的收藏夹ID列表")
    private List<String> folderIds;
}
