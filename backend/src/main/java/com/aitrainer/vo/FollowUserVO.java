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
@Schema(description = "关注用户信息")
public class FollowUserVO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户昵称")
    private String name;
    @Schema(description = "用户头像 URL")
    private String avatar;
    @Schema(description = "用户个人签名")
    private String bio;
    @Schema(description = "是否处于关注中")
    private Boolean isFollowing;
}

