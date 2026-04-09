package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AddGuestbookDTO(
        @Schema(description = "接收留言的用户ID") Long toUserId,
        @Schema(description = "留言内容") String content
) {}
