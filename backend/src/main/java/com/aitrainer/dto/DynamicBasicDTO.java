package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 动态流基础聚合对象。
 * 用于承载 SQL UNION 查询出的初步结果。
 */
@Schema(description = "动态流基础数据传输对象（用于 UNION 聚合查询）")
public record DynamicBasicDTO(
        @Schema(description = "业务记录 ID (推文 ID 或 战报 ID)")
        Long id,

        @Schema(description = "动态类型: post 或 workout_report")
        String type,

        @Schema(description = "发布时间")
        LocalDateTime createTime
) {}