-- 1. 环境准备：创建数据库并禁用外键约束检查，确保初始化顺利
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS ai_trainer;
USE ai_trainer;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. users: 用户主表（核心基础）
-- ----------------------------
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)                          NOT NULL COMMENT '登录用户名',
    email           VARCHAR(100)                         NOT NULL COMMENT '邮箱地址',
    password_hash   VARCHAR(255)                         NOT NULL COMMENT '加密密码哈希',
    avatar          VARCHAR(255)                         NULL     COMMENT '头像URL',
    is_pro          TINYINT(1) DEFAULT 0                 NULL     COMMENT '是否PRO会员',
    is_first_login  TINYINT(1) DEFAULT 1                 NULL     COMMENT '首次登录标记',
    created_at      TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at      TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NULL     ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP                            NULL,
    follower_count  INT                                  NULL     COMMENT '粉丝数量',
    following_count INT                                  NULL     COMMENT '关注数量',
    status          TINYINT    DEFAULT 0                 NULL     COMMENT '状态：0-正常, -1-已注销',
    token_version   INT        DEFAULT 0                 NULL     COMMENT '令牌版本号',
    CONSTRAINT email UNIQUE (email),
    CONSTRAINT username UNIQUE (username)
) ENGINE = InnoDB;

CREATE INDEX idx_email ON users (email);
CREATE INDEX idx_username ON users (username);

-- ----------------------------
-- 2. workouts: 健身项目配置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS workouts (
    id          VARCHAR(50)                           NOT NULL PRIMARY KEY COMMENT '项目标识',
    name        VARCHAR(100)                          NOT NULL COMMENT '中文名称',
    en_name     VARCHAR(100)                          NOT NULL COMMENT '英文名称',
    difficulty  INT         DEFAULT 1                 NULL     COMMENT '推荐难度',
    tags        JSON                                  NULL     COMMENT '项目标签',
    description TEXT                                  NULL     COMMENT '描述',
    theme_color VARCHAR(20) DEFAULT '#409EFF'         NULL     COMMENT '主题色',
    cover_url   VARCHAR(255)                          NULL     COMMENT '项目封面图URL',
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NULL,
    is_deleted  INT         DEFAULT 0                 NULL     COMMENT '逻辑删除 (0:正常, 1:已删除)'
) ENGINE = InnoDB;

-- ----------------------------
-- 3. extra_exercises: 额外运动表
-- ----------------------------
CREATE TABLE IF NOT EXISTS extra_exercises (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT                                 NOT NULL COMMENT '用户ID',
    exercise_name    VARCHAR(100)                        NOT NULL COMMENT '运动名称',
    description      VARCHAR(500)                        NULL     COMMENT '运动描述(如配速、距离等)',
    calories_burned  INT       DEFAULT 0                 NULL     COMMENT '消耗热量(kcal)',
    duration_minutes INT       DEFAULT 0                 NULL     COMMENT '运动时长(分钟)',
    exercise_date    DATE                                NOT NULL COMMENT '运动日期',
    is_deleted       TINYINT   DEFAULT 0                 NULL     COMMENT '逻辑删除(0:正常,1:已删除)',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL
);
CREATE INDEX idx_user_exercise_date ON extra_exercises (user_id, exercise_date);

-- ----------------------------
-- 4. user_profiles: 用户资料表
-- ----------------------------
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id     BIGINT                                 NOT NULL PRIMARY KEY COMMENT '用户ID，关联users表',
    nickname    VARCHAR(50)                            NOT NULL COMMENT '昵称',
    gender      VARCHAR(10)                            NOT NULL COMMENT '性别',
    goal        VARCHAR(20)                            NOT NULL COMMENT '健身目标',
    bio         VARCHAR(255)                           NULL     COMMENT '个性签名',
    height      INT                                    NULL     COMMENT '身高(cm)',
    weight      DECIMAL(5, 2)                          NULL     COMMENT '体重(kg)',
    body_fat    DECIMAL(4, 2)                          NULL     COMMENT '体脂率(%)',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    total_likes INT UNSIGNED DEFAULT '0'               NULL,
    age         INT                                    NULL     COMMENT '用户年龄'
) COMMENT '用户资料表' ENGINE = InnoDB COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- 5. user_privacy_settings: 用户隐私设置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS user_privacy_settings (
    user_id          BIGINT                               NOT NULL PRIMARY KEY COMMENT '用户ID',
    public_ai_report TINYINT(1) DEFAULT 1                 NOT NULL COMMENT '是否公开展示AI战报 (1:公开, 0:私密)',
    updated_at       DATETIME   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户隐私设置表' ENGINE = InnoDB;

-- ----------------------------
-- 6. ai_coach_chat_history: AI 教练聊天历史表
-- ----------------------------
CREATE TABLE IF NOT EXISTS ai_coach_chat_history (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    user_id       BIGINT                             NOT NULL COMMENT '用户 ID',
    session_id    VARCHAR(100)                       NOT NULL COMMENT '会话 ID',
    role          VARCHAR(20)                        NOT NULL COMMENT '消息角色：user-用户消息, assistant-AI回复',
    content       TEXT                               NOT NULL COMMENT '消息内容',
    analysis_type VARCHAR(50)                        NULL     COMMENT '分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析, chat-闲聊',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    reply_to      BIGINT                             NULL     COMMENT '关联提问的消息 ID'
) COMMENT 'AI 教练聊天历史表' ENGINE = InnoDB COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_created_at ON ai_coach_chat_history (created_at);
CREATE INDEX idx_reply_to ON ai_coach_chat_history (reply_to);
CREATE INDEX idx_session_id ON ai_coach_chat_history (session_id);
CREATE INDEX idx_user_id ON ai_coach_chat_history (user_id);
CREATE INDEX idx_user_session ON ai_coach_chat_history (user_id, session_id);

-- ----------------------------
-- 7. collection_folder: 收藏夹表
-- ----------------------------
CREATE TABLE IF NOT EXISTS collection_folder (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id     BIGINT                             NOT NULL COMMENT '所属用户ID',
    name        VARCHAR(100)                       NOT NULL COMMENT '收藏夹名称',
    is_public   TINYINT  DEFAULT 0                 NULL     COMMENT '是否公开：0-私密，1-公开',
    is_default  TINYINT  DEFAULT 0                 NULL     COMMENT '是否为默认收藏夹：0-普通，1-默认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL     COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT  DEFAULT 0                 NULL     COMMENT '逻辑删除：0-未删除，1-已删除'
) COMMENT '收藏夹表' ENGINE = InnoDB COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_user_id ON collection_folder (user_id);

-- ----------------------------
-- 8. community_guestbook: 用户空间留言板
-- ----------------------------
CREATE TABLE IF NOT EXISTS community_guestbook (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    from_user_id  BIGINT                               NOT NULL COMMENT '留言者ID',
    to_user_id    BIGINT                               NOT NULL COMMENT '被留言者（空间主人）ID',
    content       VARCHAR(500)                         NOT NULL COMMENT '留言内容',
    create_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '留言时间',
    reply_content VARCHAR(500)                         NULL     COMMENT '主人回复内容',
    reply_time    DATETIME                             NULL     COMMENT '回复时间',
    is_deleted    TINYINT(1) DEFAULT 0                 NOT NULL COMMENT '逻辑删除 (0:未删, 1:已删)',
    update_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户空间留言板' ENGINE = InnoDB;

CREATE INDEX idx_from_user ON community_guestbook (from_user_id);
CREATE INDEX idx_to_user ON community_guestbook (to_user_id);

-- ----------------------------
-- 9. workout_session_likes: 战报点赞关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS workout_session_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT                             NOT NULL COMMENT '战报ID',
    user_id    BIGINT                             NOT NULL COMMENT '点赞用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NULL     COMMENT '点赞时间',
    CONSTRAINT uk_session_user UNIQUE (session_id, user_id) COMMENT '唯一索引，防止重复点赞'
) COMMENT '战报点赞关联表' ENGINE = InnoDB;

-- ----------------------------
-- 10. meals: 饮食表（依赖 users）
-- ----------------------------
CREATE TABLE IF NOT EXISTS meals (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT                                            NOT NULL COMMENT '用户ID',
    meal_time  DATETIME                                       NOT NULL COMMENT '进餐时间',
    meal_type  ENUM ('breakfast', 'lunch', 'dinner', 'snack') NOT NULL COMMENT '餐次类型',
    food_name  TEXT                                           NOT NULL COMMENT '食物名称',
    calories   INT            DEFAULT 0                       NULL     COMMENT '热量',
    tag_type   VARCHAR(20)    DEFAULT 'info'                  NULL     COMMENT '标签样式',
    weight     INT            DEFAULT 0                       NULL     COMMENT '食物重量(g)',
    protein    DECIMAL(10, 2) DEFAULT 0.00                    NULL     COMMENT '蛋白质含量(g)',
    fat        DECIMAL(10, 2) DEFAULT 0.00                    NULL     COMMENT '脂肪含量(g)',
    carbs      DECIMAL(10, 2) DEFAULT 0.00                    NULL     COMMENT '碳水化合物含量(g)',
    is_deleted TINYINT        DEFAULT 0                       NULL     COMMENT '逻辑删除(0:正常,1:已删除)',
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP       NULL,
    CONSTRAINT meals_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_user_meals ON meals (user_id, meal_time);

-- ----------------------------
-- 11. user_follows: 用户关注表（依赖 users）
-- ----------------------------
CREATE TABLE IF NOT EXISTS user_follows (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT                                 NOT NULL COMMENT '关注者ID',
    followed_id INT                                 NOT NULL COMMENT '被关注者ID',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT uk_follower_followed UNIQUE (follower_id, followed_id),
    CONSTRAINT user_follows_ibfk_1 FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_follows_ibfk_2 FOREIGN KEY (followed_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_followed ON user_follows (followed_id);
CREATE INDEX idx_follower ON user_follows (follower_id);

-- ----------------------------
-- 12. workout_sessions: 训练战报表（依赖 users, workouts）
-- ----------------------------
CREATE TABLE IF NOT EXISTS workout_sessions (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT                                 NOT NULL COMMENT '用户ID',
    workout_id       VARCHAR(50)                         NOT NULL COMMENT '项目ID',
    score            INT                                 NOT NULL COMMENT '综合评分',
    grade            VARCHAR(5)                          NOT NULL COMMENT '评级',
    grade_level      VARCHAR(20)                         NULL     COMMENT '评级样式类',
    comment          TEXT                                NULL     COMMENT 'AI点评',
    valid_reps       INT       DEFAULT 0                 NULL     COMMENT '有效次数',
    invalid_reps     INT       DEFAULT 0                 NULL     COMMENT '异常次数',
    duration_seconds INT       DEFAULT 0                 NULL     COMMENT '训练时长(秒)',
    calories_burned  INT       DEFAULT 0                 NULL     COMMENT '消耗热量',
    like_count       INT       DEFAULT 0                 NULL     COMMENT '获赞总数',
    radar_scores     JSON                                NULL     COMMENT '五维评分',
    snapshots        JSON                                NULL     COMMENT '纠错抓拍',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    is_deleted       TINYINT   DEFAULT 0                 NULL,
    CONSTRAINT workout_sessions_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT workout_sessions_ibfk_2 FOREIGN KEY (workout_id) REFERENCES workouts (id)
) ENGINE = InnoDB;

CREATE INDEX idx_user_workouts ON workout_sessions (user_id, created_at);
CREATE INDEX workout_id ON workout_sessions (workout_id);

-- ----------------------------
-- 13. collection_item: 收藏明细表
-- ----------------------------
CREATE TABLE IF NOT EXISTS collection_item (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    folder_id   BIGINT                             NOT NULL COMMENT '收藏夹ID',
    post_id     BIGINT                             NOT NULL COMMENT '被收藏的推文ID',
    user_id     BIGINT                             NOT NULL COMMENT '所属用户ID（冗余字段）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL     COMMENT '收藏时间',
    is_deleted  TINYINT  DEFAULT 0                 NULL,
    CONSTRAINT uk_folder_post UNIQUE (folder_id, post_id)
) COMMENT '收藏明细表' ENGINE = InnoDB COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_folder_id ON collection_item (folder_id);
CREATE INDEX idx_user_post ON collection_item (user_id, post_id);

-- ----------------------------
-- 14. community_posts: 社区动态表（依赖 users, workout_sessions）
-- ----------------------------
CREATE TABLE IF NOT EXISTS community_posts (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT                                   NOT NULL COMMENT '发布者ID',
    content        TEXT                                  NOT NULL COMMENT '内容',
    topic          VARCHAR(50)                           NULL     COMMENT '话题',
    device         VARCHAR(50) DEFAULT 'AiTrainer App'   NULL     COMMENT '发布设备',
    ai_report_id   INT                                   NULL     COMMENT '关联战报ID',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NULL     ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     TIMESTAMP                             NULL,
    like_count     INT         DEFAULT 0                 NULL,
    favorite_count INT         DEFAULT 0                 NULL,
    comment_count  INT         DEFAULT 0                 NULL,
    is_deleted     TINYINT     DEFAULT 0                 NULL,
    CONSTRAINT community_posts_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT community_posts_ibfk_2 FOREIGN KEY (ai_report_id) REFERENCES workout_sessions (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_ai_report_id ON community_posts (ai_report_id);
CREATE INDEX idx_post_created_at ON community_posts (created_at);
CREATE INDEX idx_topic_created ON community_posts (topic, created_at);
CREATE INDEX idx_user_posts ON community_posts (user_id, created_at);

-- ----------------------------
-- 15. post_images: 动态图片表（依赖 community_posts）
-- ----------------------------
CREATE TABLE IF NOT EXISTS post_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT                                 NOT NULL COMMENT '关联动态ID',
    object_key VARCHAR(255)                        NOT NULL COMMENT 'OSS 对象 Key',
    sort_order INT       DEFAULT 0                 NULL     COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT post_images_ibfk_1 FOREIGN KEY (post_id) REFERENCES community_posts (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_post_images ON post_images (post_id, sort_order);

-- ----------------------------
-- 16. post_likes: 动态点赞表（依赖 community_posts, users）
-- ----------------------------
CREATE TABLE IF NOT EXISTS post_likes (
    post_id    INT                                 NOT NULL COMMENT '动态ID',
    user_id    INT                                 NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT post_likes_ibfk_1 FOREIGN KEY (post_id) REFERENCES community_posts (id) ON DELETE CASCADE,
    CONSTRAINT post_likes_ibfk_2 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_like_user_id ON post_likes (user_id);

-- ----------------------------
-- 17. post_favorites: 动态收藏关联表（依赖 community_posts, users）
-- ----------------------------
CREATE TABLE IF NOT EXISTS post_favorites (
    post_id    INT                                 NOT NULL COMMENT '动态ID',
    user_id    INT                                 NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT post_favorites_ibfk_1 FOREIGN KEY (post_id) REFERENCES community_posts (id) ON DELETE CASCADE,
    CONSTRAINT post_favorites_ibfk_2 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_fav_user_id ON post_favorites (user_id);

-- ----------------------------
-- 18. post_comments: 动态评论表（含自关联）
-- ----------------------------
CREATE TABLE IF NOT EXISTS post_comments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT                                 NOT NULL COMMENT '动态ID',
    user_id    INT                                 NOT NULL COMMENT '用户ID',
    parent_id  INT                                 NULL     COMMENT '父级评论ID',
    content    TEXT                                NOT NULL COMMENT '内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    deleted_at TIMESTAMP                           NULL,
    is_deleted TINYINT   DEFAULT 0                 NULL,
    CONSTRAINT post_comments_ibfk_1 FOREIGN KEY (post_id) REFERENCES community_posts (id) ON DELETE CASCADE,
    CONSTRAINT post_comments_ibfk_2 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT post_comments_ibfk_3 FOREIGN KEY (parent_id) REFERENCES post_comments (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_post_comments ON post_comments (post_id, created_at);
CREATE INDEX idx_comment_parent_id ON post_comments (parent_id);
CREATE INDEX idx_comment_user_id ON post_comments (user_id);

-- 最后：恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO workouts (id, name, en_name, difficulty, tags, description, theme_color, cover_url, created_at, is_deleted) VALUES ('good_morning', '早安式体前屈', 'Good Morning', 4, '["核心", "腘绳肌"]', '强化下背部与核心稳定，极度依赖动作标准度，AI 严苛监测脊柱中立位。', '#E6A23C', 'https://my-bucket2026.oss-cn-beijing.aliyuncs.com/workout_covers/Good Morning.png', '2026-03-15 20:48:22', 0);
INSERT INTO workouts (id, name, en_name, difficulty, tags, description, theme_color, cover_url, created_at, is_deleted) VALUES ('lunge', 'AI 箭步蹲', 'Lunge', 2, '["单边控制", "塑形"]', '改善左右发力不均，精准打击臀大肌下沿，AI 严控前膝过伸问题。', '#67C23A', 'https://my-bucket2026.oss-cn-beijing.aliyuncs.com/workout_covers/Lunge.png', '2026-03-15 20:48:22', 0);
INSERT INTO workouts (id, name, en_name, difficulty, tags, description, theme_color, cover_url, created_at, is_deleted) VALUES ('squat', 'AI 深蹲', 'Squat', 3, '["臀腿", "力量"]', '全面激活下肢力量，AI 实时监测膝盖轨迹、髋部深度与背部角度。', '#409EFF', 'https://my-bucket2026.oss-cn-beijing.aliyuncs.com/workout_covers/Squat.jpg', '2026-03-15 20:48:22', 0);
