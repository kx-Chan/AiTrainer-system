-- AI 教练聊天历史表
-- 用于存储用户与 AI 教练的对话记录，实现上下文功能

CREATE TABLE IF NOT EXISTS `ai_coach_chat_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `session_id` VARCHAR(100) NOT NULL COMMENT '会话 ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：user-用户消息, assistant-AI回复',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `analysis_type` VARCHAR(50) DEFAULT NULL COMMENT '分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_session` (`user_id`, `session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 教练聊天历史表';
