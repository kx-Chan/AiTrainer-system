package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "社区推文视图对象")
public class CommunityPostVO {

    @Schema(description = "推文ID")
    private Long id;

    @Schema(description = "作者展示名")
    private String author;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者头像")
    private String avatar;

    @Schema(description = "是否 PRO")
    private Boolean isPro;

    @Schema(description = "展示时间文案")
    private LocalDateTime time;

    @Schema(description = "发布设备")
    private String device;

    @Schema(description = "话题（带 #）")
    private String topic;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "评论数")
    private Integer comments;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "收藏数")
    private Integer favorites;

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "是否已关注作者")
    private Boolean isFollowing;

    @Schema(description = "帖子图片临时访问 URL 列表")
    private List<String> images;

    @Schema(description = "关联的 AI 战报详情（如果不带战报发帖则为 null）")
    private WorkoutSessionVO aiReport;

    @Schema(description = "关联的战报 ID（可选，方便前端逻辑判断）")
    private Long workoutSessionId;
}
