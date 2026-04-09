package com.aitrainer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页查询结果")
public class PageResultVO<T> {
    @Schema(description = "数据记录列表")
    private List<T> records;
    @Schema(description = "总记录数")
    private long total;
    @Schema(description = "当前页码")
    private long page;
    @Schema(description = "每页条数")
    private long size;

    /**
     * 返回空的分页结果
     */
    public static <T> PageResultVO<T> empty(long page, long size) {
        return PageResultVO.<T>builder()
                .records(java.util.Collections.emptyList())
                .total(0L)
                .page(page)
                .size(size)
                .build();
    }
}

