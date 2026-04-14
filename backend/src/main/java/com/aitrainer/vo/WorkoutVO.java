package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "健身项目详情")
public class WorkoutVO {

    @Schema(description = "项目唯一标识 (如: squat, lunge)")
    private String id;

    @Schema(description = "项目中文名称")
    private String name;

    @Schema(description = "项目英文名称")
    private String enName;

    @Schema(description = "训练难度 (1-5星)")
    private Integer difficulty;

    @Schema(description = "项目标签 (解析后的列表)")
    private List<String> tags;

    @Schema(description = "项目详细描述")
    private String description;

    @Schema(description = "前端渲染的主题色 (十六进制)")
    private String themeColor;

    @Schema(description = "封面图地址")
    private String coverUrl;
}