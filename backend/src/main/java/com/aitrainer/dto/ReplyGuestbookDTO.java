package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReplyGuestbookDTO(
        @Schema(description = "回复内容") String replyContent
) {}
