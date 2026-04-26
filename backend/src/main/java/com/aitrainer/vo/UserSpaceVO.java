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
@Schema(description = "用户空间个人资料视图对象")
public class UserSpaceVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "个性签名")
    private String bio;

    @Schema(description = "健身目标", example = "增肌")
    private String goal;

    @Schema(description = "是否为 PRO 用户")
    private Boolean isPro;

    @Schema(description = "关注数量")
    private Integer followingCount;

    @Schema(description = "粉丝数量")
    private Integer followerCount;

    @Schema(description = "累计获赞总数")
    private Long totalLikes;

    @Schema(description = "当前登录用户是否已关注此人")
    private Boolean isFollowing;

    @Schema(description = "用户状态：-1表示已注销")
    private Integer status;

    @Schema(description = "是否已注销")
    private Boolean deactivated;
}
