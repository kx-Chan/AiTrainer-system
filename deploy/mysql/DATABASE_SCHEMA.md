# AiTrainer 数据库文档

> 基于初始化脚本生成：`deploy/mysql/init/init.sql`  
> 数据库：`ai_trainer`  
> 字符集：`utf8mb4`  

## 1. 概览

本库用于支撑 AiTrainer 的用户体系、训练项目与战报、社区互动（动态/评论/点赞/收藏）、AI 教练聊天历史、饮食记录等核心业务。

### 1.1 设计约定

- 多数业务表包含逻辑删除字段（如 `is_deleted`、`deleted_at`），读写时需注意过滤。
- 时间字段常见为 `created_at / updated_at` 或 `create_time / update_time`，均用于审计与排序。

## 2. 表清单

| 序号 | 表名 | 中文说明 |
| --- | --- | --- |
| 1 | users | 用户主表（核心基础） |
| 2 | workouts | 健身项目配置表 |
| 3 | extra_exercises | 额外运动表 |
| 4 | user_profiles | 用户资料表 |
| 5 | user_privacy_settings | 用户隐私设置表 |
| 6 | ai_coach_chat_history | AI 教练聊天历史表 |
| 7 | collection_folder | 收藏夹表 |
| 8 | community_guestbook | 用户空间留言板 |
| 9 | workout_session_likes | 战报点赞关联表 |
| 10 | meals | 饮食表 |
| 11 | user_follows | 用户关注表 |
| 12 | workout_sessions | 训练战报表 |
| 13 | collection_item | 收藏明细表 |
| 14 | community_posts | 社区动态表 |
| 15 | post_images | 动态图片表 |
| 16 | post_likes | 动态点赞表 |
| 17 | post_favorites | 动态收藏关联表 |
| 18 | post_comments | 动态评论表（含自关联） |

## 3. 表结构明细

### 3.1 users（用户主表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| username | VARCHAR(50) | 否 | - | 登录用户名 |
| email | VARCHAR(100) | 否 | - | 邮箱地址 |
| password_hash | VARCHAR(255) | 否 | - | 加密密码哈希 |
| avatar | VARCHAR(255) | 是 | NULL | 头像URL |
| is_pro | TINYINT(1) | 是 | 0 | 是否PRO会员 |
| is_first_login | TINYINT(1) | 是 | 1 | 首次登录标记 |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |
| deleted_at | TIMESTAMP | 是 | NULL | 删除时间 |
| follower_count | INT | 是 | NULL | 粉丝数量 |
| following_count | INT | 是 | NULL | 关注数量 |
| status | TINYINT | 是 | 0 | 状态：0-正常, -1-已注销 |
| token_version | INT | 是 | 0 | 令牌版本号 |

**约束与索引**

- 唯一约束：`email`、`username`
- 索引：`idx_email(email)`、`idx_username(username)`

---

### 3.2 workouts（健身项目配置表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | VARCHAR(50) | 否 | - | 项目标识（主键） |
| name | VARCHAR(100) | 否 | - | 中文名称 |
| en_name | VARCHAR(100) | 否 | - | 英文名称 |
| difficulty | INT | 是 | 1 | 推荐难度 |
| tags | JSON | 是 | NULL | 项目标签 |
| description | TEXT | 是 | NULL | 描述 |
| theme_color | VARCHAR(20) | 是 | #409EFF | 主题色 |
| cover_url | VARCHAR(255) | 是 | NULL | 项目封面图URL |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| is_deleted | INT | 是 | 0 | 逻辑删除 (0:正常, 1:已删除) |

---

### 3.3 extra_exercises（额外运动表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| user_id | INT | 否 | - | 用户ID |
| exercise_name | VARCHAR(100) | 否 | - | 运动名称 |
| description | VARCHAR(500) | 是 | NULL | 运动描述(如配速、距离等) |
| calories_burned | INT | 是 | 0 | 消耗热量(kcal) |
| duration_minutes | INT | 是 | 0 | 运动时长(分钟) |
| exercise_date | DATE | 否 | - | 运动日期 |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除(0:正常,1:已删除) |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**索引**

- `idx_user_exercise_date(user_id, exercise_date)`

---

### 3.4 user_profiles（用户资料表）

> 表注释：用户资料表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| user_id | BIGINT | 否 | - | 用户ID，关联users表（主键） |
| nickname | VARCHAR(50) | 否 | - | 昵称 |
| gender | VARCHAR(10) | 否 | - | 性别 |
| goal | VARCHAR(20) | 否 | - | 健身目标 |
| bio | VARCHAR(255) | 是 | NULL | 个性签名 |
| height | INT | 是 | NULL | 身高(cm) |
| weight | DECIMAL(5,2) | 是 | NULL | 体重(kg) |
| body_fat | DECIMAL(4,2) | 是 | NULL | 体脂率(%) |
| updated_at | DATETIME | 否 | CURRENT_TIMESTAMP | 更新时间（自动更新） |
| total_likes | INT UNSIGNED | 是 | 0 | 总获赞数 |
| age | INT | 是 | NULL | 用户年龄 |

---

### 3.5 user_privacy_settings（用户隐私设置表）

> 表注释：用户隐私设置表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| user_id | BIGINT | 否 | - | 用户ID（主键） |
| public_ai_report | TINYINT(1) | 否 | 1 | 是否公开展示AI战报 (1:公开, 0:私密) |
| updated_at | DATETIME | 否 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 3.6 ai_coach_chat_history（AI 教练聊天历史表）

> 表注释：AI 教练聊天历史表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键 ID |
| user_id | BIGINT | 否 | - | 用户 ID |
| session_id | VARCHAR(100) | 否 | - | 会话 ID |
| role | VARCHAR(20) | 否 | - | 消息角色：user-用户消息, assistant-AI回复 |
| content | TEXT | 否 | - | 消息内容 |
| analysis_type | VARCHAR(50) | 是 | NULL | 分析类型：training/diet/comprehensive/chat |
| created_at | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| reply_to | BIGINT | 是 | NULL | 关联提问的消息 ID |

**索引**

- `idx_created_at(created_at)`
- `idx_reply_to(reply_to)`
- `idx_session_id(session_id)`
- `idx_user_id(user_id)`
- `idx_user_session(user_id, session_id)`

---

### 3.7 collection_folder（收藏夹表）

> 表注释：收藏夹表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键ID |
| user_id | BIGINT | 否 | - | 所属用户ID |
| name | VARCHAR(100) | 否 | - | 收藏夹名称 |
| is_public | TINYINT | 是 | 0 | 是否公开：0-私密，1-公开 |
| is_default | TINYINT | 是 | 0 | 是否为默认收藏夹：0-普通，1-默认 |
| create_time | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |

**索引**

- `idx_user_id(user_id)`

---

### 3.8 community_guestbook（用户空间留言板）

> 表注释：用户空间留言板

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键ID |
| from_user_id | BIGINT | 否 | - | 留言者ID |
| to_user_id | BIGINT | 否 | - | 被留言者（空间主人）ID |
| content | VARCHAR(500) | 否 | - | 留言内容 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 留言时间 |
| reply_content | VARCHAR(500) | 是 | NULL | 主人回复内容 |
| reply_time | DATETIME | 是 | NULL | 回复时间 |
| is_deleted | TINYINT(1) | 否 | 0 | 逻辑删除 (0:未删, 1:已删) |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

**索引**

- `idx_from_user(from_user_id)`
- `idx_to_user(to_user_id)`

---

### 3.9 workout_session_likes（战报点赞关联表）

> 表注释：战报点赞关联表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键 |
| session_id | BIGINT | 否 | - | 战报ID |
| user_id | BIGINT | 否 | - | 点赞用户ID |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 点赞时间 |

**约束**

- 唯一约束：`uk_session_user(session_id, user_id)`（防止重复点赞）

---

### 3.10 meals（饮食表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| user_id | INT | 否 | - | 用户ID |
| meal_time | DATETIME | 否 | - | 进餐时间 |
| meal_type | ENUM('breakfast','lunch','dinner','snack') | 否 | - | 餐次类型 |
| food_name | TEXT | 否 | - | 食物名称 |
| calories | INT | 是 | 0 | 热量 |
| tag_type | VARCHAR(20) | 是 | info | 标签样式 |
| weight | INT | 是 | 0 | 食物重量(g) |
| protein | DECIMAL(10,2) | 是 | 0.00 | 蛋白质含量(g) |
| fat | DECIMAL(10,2) | 是 | 0.00 | 脂肪含量(g) |
| carbs | DECIMAL(10,2) | 是 | 0.00 | 碳水化合物含量(g) |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除(0:正常,1:已删除) |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**外键**

- `meals.user_id` → `users.id`（ON DELETE CASCADE）

**索引**

- `idx_user_meals(user_id, meal_time)`

---

### 3.11 user_follows（用户关注表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键 |
| follower_id | INT | 否 | - | 关注者ID |
| followed_id | INT | 否 | - | 被关注者ID |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**约束与外键**

- 唯一约束：`uk_follower_followed(follower_id, followed_id)`
- 外键：`follower_id` → `users.id`（ON DELETE CASCADE）
- 外键：`followed_id` → `users.id`（ON DELETE CASCADE）

**索引**

- `idx_followed(followed_id)`
- `idx_follower(follower_id)`

---

### 3.12 workout_sessions（训练战报表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| user_id | INT | 否 | - | 用户ID |
| workout_id | VARCHAR(50) | 否 | - | 项目ID |
| score | INT | 否 | - | 综合评分 |
| grade | VARCHAR(5) | 否 | - | 评级 |
| grade_level | VARCHAR(20) | 是 | NULL | 评级样式类 |
| comment | TEXT | 是 | NULL | AI点评 |
| valid_reps | INT | 是 | 0 | 有效次数 |
| invalid_reps | INT | 是 | 0 | 异常次数 |
| duration_seconds | INT | 是 | 0 | 训练时长(秒) |
| calories_burned | INT | 是 | 0 | 消耗热量 |
| like_count | INT | 是 | 0 | 获赞总数 |
| radar_scores | JSON | 是 | NULL | 五维评分 |
| snapshots | JSON | 是 | NULL | 纠错抓拍 |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除 |

**外键**

- `user_id` → `users.id`（ON DELETE CASCADE）
- `workout_id` → `workouts.id`

**索引**

- `idx_user_workouts(user_id, created_at)`
- `workout_id(workout_id)`

---

### 3.13 collection_item（收藏明细表）

> 表注释：收藏明细表

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键ID |
| folder_id | BIGINT | 否 | - | 收藏夹ID |
| post_id | BIGINT | 否 | - | 被收藏的推文ID |
| user_id | BIGINT | 否 | - | 所属用户ID（冗余字段） |
| create_time | DATETIME | 是 | CURRENT_TIMESTAMP | 收藏时间 |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除 |

**约束与索引**

- 唯一约束：`uk_folder_post(folder_id, post_id)`
- 索引：`idx_folder_id(folder_id)`、`idx_user_post(user_id, post_id)`

---

### 3.14 community_posts（社区动态表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| user_id | INT | 否 | - | 发布者ID |
| content | TEXT | 否 | - | 内容 |
| topic | VARCHAR(50) | 是 | NULL | 话题 |
| device | VARCHAR(50) | 是 | AiTrainer App | 发布设备 |
| ai_report_id | INT | 是 | NULL | 关联战报ID |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |
| deleted_at | TIMESTAMP | 是 | NULL | 删除时间 |
| like_count | INT | 是 | 0 | 点赞数 |
| favorite_count | INT | 是 | 0 | 收藏数 |
| comment_count | INT | 是 | 0 | 评论数 |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除 |

**外键**

- `user_id` → `users.id`（ON DELETE CASCADE）
- `ai_report_id` → `workout_sessions.id`（ON DELETE SET NULL）

**索引**

- `idx_ai_report_id(ai_report_id)`
- `idx_post_created_at(created_at)`
- `idx_topic_created(topic, created_at)`
- `idx_user_posts(user_id, created_at)`

---

### 3.15 post_images（动态图片表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 否 | 自增 | 主键 |
| post_id | INT | 否 | - | 关联动态ID |
| object_key | VARCHAR(255) | 否 | - | OSS 对象 Key |
| sort_order | INT | 是 | 0 | 排序 |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**外键**

- `post_id` → `community_posts.id`（ON DELETE CASCADE）

**索引**

- `idx_post_images(post_id, sort_order)`

---

### 3.16 post_likes（动态点赞表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| post_id | INT | 否 | - | 动态ID（联合主键） |
| user_id | INT | 否 | - | 用户ID（联合主键） |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**主键与外键**

- 主键：`(post_id, user_id)`
- 外键：`post_id` → `community_posts.id`（ON DELETE CASCADE）
- 外键：`user_id` → `users.id`（ON DELETE CASCADE）

**索引**

- `idx_like_user_id(user_id)`

---

### 3.17 post_favorites（动态收藏关联表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| post_id | INT | 否 | - | 动态ID（联合主键） |
| user_id | INT | 否 | - | 用户ID（联合主键） |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |

**主键与外键**

- 主键：`(post_id, user_id)`
- 外键：`post_id` → `community_posts.id`（ON DELETE CASCADE）
- 外键：`user_id` → `users.id`（ON DELETE CASCADE）

---

### 3.18 post_comments（动态评论表）

**字段**

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | INT | 否 | 自增 | 主键 |
| post_id | INT | 否 | - | 动态ID |
| user_id | INT | 否 | - | 用户ID |
| parent_id | INT | 是 | NULL | 父级评论ID |
| content | TEXT | 否 | - | 内容 |
| created_at | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| deleted_at | TIMESTAMP | 是 | NULL | 删除时间 |
| is_deleted | TINYINT | 是 | 0 | 逻辑删除 |

**外键**

- `post_id` → `community_posts.id`（ON DELETE CASCADE）
- `user_id` → `users.id`（ON DELETE CASCADE）
- `parent_id` → `post_comments.id`（ON DELETE CASCADE，自关联）

**索引**

- `idx_post_comments(post_id, created_at)`
- `idx_comment_parent_id(parent_id)`
- `idx_comment_user_id(user_id)`

## 4. 外键关系汇总

| 子表.字段 | 父表.字段 | 删除策略 |
| --- | --- | --- |
| meals.user_id | users.id | CASCADE |
| user_follows.follower_id | users.id | CASCADE |
| user_follows.followed_id | users.id | CASCADE |
| workout_sessions.user_id | users.id | CASCADE |
| workout_sessions.workout_id | workouts.id | RESTRICT/默认 |
| community_posts.user_id | users.id | CASCADE |
| community_posts.ai_report_id | workout_sessions.id | SET NULL |
| post_images.post_id | community_posts.id | CASCADE |
| post_likes.post_id | community_posts.id | CASCADE |
| post_likes.user_id | users.id | CASCADE |
| post_favorites.post_id | community_posts.id | CASCADE |
| post_favorites.user_id | users.id | CASCADE |
| post_comments.post_id | community_posts.id | CASCADE |
| post_comments.user_id | users.id | CASCADE |
| post_comments.parent_id | post_comments.id | CASCADE |

## 5. 初始化数据

初始化脚本会向 `workouts` 插入 3 条示例项目数据：

- `good_morning`（早安式体前屈）
- `lunge`（AI 箭步蹲）
- `squat`（AI 深蹲）
