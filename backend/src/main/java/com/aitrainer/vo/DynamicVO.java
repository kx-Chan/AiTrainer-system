package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicVO {
    @Schema(description = "动态ID")
    private Long id;

    @Schema(description = "动态类型: post 或 workout_report")
    private String type;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    // --- 负载数据 ---

    @Schema(description = "推文详情（当 type 为 post 时填充）")
    private CommunityPostVO post;

    @Schema(description = "AI战报详情（当 type 为 workout_report 时填充）")
    private WorkoutSessionVO aiReport; // 前端明确在找这个字段名
}
