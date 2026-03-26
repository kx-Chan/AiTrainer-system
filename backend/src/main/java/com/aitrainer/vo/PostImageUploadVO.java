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
@Schema(description = "帖子图片上传返回结果")
public class PostImageUploadVO {
    @Schema(description = "OSS 对象 Key")
    private String key;
    @Schema(description = "临时访问 URL")
    private String url;
}
