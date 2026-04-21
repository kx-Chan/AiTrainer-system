package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 饮食记录 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "饮食记录")
public class MealVO {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "进餐时间 (HH:mm)")
    private String time;

    @Schema(description = "餐次类型中文名")
    private String type;

    @Schema(description = "餐次类型英文标识")
    private String mealType;

    @Schema(description = "食物名称")
    private String foodName;

    @Schema(description = "热量 (kcal)")
    private Integer calories;

    @Schema(description = "蛋白质 (g)")
    private Integer protein;

    @Schema(description = "脂肪 (g)")
    private Integer fat;

    @Schema(description = "碳水化合物 (g)")
    private Integer carbs;

    @Schema(description = "食物重量 (g)")
    private Integer weight;

    @Schema(description = "标签样式")
    private String tagType;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
