package com.aitrainer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户个人主页顶部展示的资料信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "返回用户个人信息")
public class UserProfileVO {

    /**
     * 用户 ID。
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 昵称。
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 性别。
     */
    @Schema(description = "用户性别")
    private String gender;

    /**
     * 健身目标（减脂、增肌、保持）。
     */
    @Schema(description = "健身目标（减脂、增肌、保持）")
    private String goal;

    /**
     * 个性签名。
     */
    @Schema(description = "用户个人签名")
    private String bio;

    /**
     * 身高 (cm)。
     */
    @Schema(description = "用户身高 (cm)")
    private Integer height;

    /**
     * 体重 (kg)。
     */
    @Schema(description = "用户体重")
    private BigDecimal weight;

    /**
     * 体脂率 (%)。
     */
    @Schema(description = "用户体脂率 (%)")
    private BigDecimal bodyFat;

    /**
     * 用户头像 URL 。
     */
    @Schema(description = "用户头像 URL")
    private String avatar;

    /**
     * 关注数 (模拟)。
     */
    @Schema(description = "关注数")
    @Builder.Default
    private Integer following = 0;

    /**
     * 粉丝数 (模拟)。
     */
    @Schema(description = "粉丝数")
    @Builder.Default
    private Integer followers = 0;

    /**
     * 累计打卡天数 (模拟)。
     */
    @Schema(description = "累计获赞")
    @Builder.Default
    private Long totalLikes = 0L;
}
