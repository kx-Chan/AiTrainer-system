package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏夹列表项")
public class FolderVO {
    @Schema(description = "收藏夹ID")
    private Long id;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "是否为默认收藏夹")
    private Integer isDefault;

    @Schema(description = "收藏夹内推文总数")
    private Integer itemCount;

    @Schema(description = "是否是公开收藏夹")
    private Integer isPublic;

    @Schema(description = "所有者用户ID")
    private Long userId;
}