package com.aitrainer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户个人主页顶部展示的资料信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 性别。
     */
    private String gender;

    /**
     * 健身目标（减脂、增肌、保持）。
     */
    private String goal;

    /**
     * 个性签名。
     */
    private String bio;

    /**
     * 身高 (cm)。
     */
    private Integer height;

    /**
     * 体重 (kg)。
     */
    private BigDecimal weight;

    /**
     * 体脂率 (%)。
     */
    private BigDecimal bodyFat;

    /**
     * 用户头像 URL (后期扩展)。
     */
    private String avatar;

    /**
     * 关注数 (模拟)。
     */
    @Builder.Default
    private Integer following = 0;

    /**
     * 粉丝数 (模拟)。
     */
    @Builder.Default
    private Integer followers = 0;

    /**
     * 累计打卡天数 (模拟)。
     */
    @Builder.Default
    private Integer totalDays = 0;
}
