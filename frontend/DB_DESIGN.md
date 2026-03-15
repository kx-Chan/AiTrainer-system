# AiTrainer 数据库设计文档 (v3.0)

## 1. 概述
本数据库设计旨在支持 AiTrainer 平台的核心业务需求。文档包含了 12 张核心数据表的详细字段定义、业务含义及关联关系，涵盖了从用户管理、身体指标、AI 训练、社交互动到饮食追踪的所有模块。

## 2. 数据命名与规范
- **表名**: 使用复数形式，蛇形命名法 (`snake_case`)，如 `community_posts`。
- **字段名**: 蛇形命名法，见名知义。
- **基础字段标准**:
  - `id`: 主键，统一使用自增整数或特定业务字符串。
  - `created_at`: 记录创建时间。
  - `updated_at`: 记录最后一次更新时间。
  - `deleted_at`: 软删除标记（`NULL` 表示未删除）。
- **字段类型标准**:
  - 金额/权重/身高: 使用 `DECIMAL` 保证精度。
  - 开关/布尔值: 使用 `BOOLEAN` 或 `TINYINT(1)`。
  - 复杂数据结构（如评分、图片列表）: 使用 `JSON` 字段。

---

## 3. 详细数据表定义

### 3.1 用户模块 (Users & Profiles)

#### 3.1.1 `users` (核心用户账号表)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 用户唯一识别 ID | PK, AI |
| username | VARCHAR(50) | 登录用户名 | UNIQUE, NOT NULL |
| email | VARCHAR(100) | 邮箱地址 | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | 加密后的密码哈希 | NOT NULL |
| avatar | VARCHAR(255) | 用户头像 URL | |
| is_pro | BOOLEAN | 是否为 PRO 会员 | DEFAULT FALSE |
| is_first_login | BOOLEAN | 是否首次登录（用于引导流程控制） | DEFAULT TRUE |
| created_at | TIMESTAMP | 注册时间 | |
| updated_at | TIMESTAMP | 资料更新时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

#### 3.1.2 `user_profiles` (用户物理特征与目标)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| user_id | INT | 关联的用户 ID | PK, FK (users.id) |
| gender | ENUM | 性别 (male, female, other) | NOT NULL |
| height | DECIMAL(5,2) | 身高 (cm) | NOT NULL |
| weight | DECIMAL(5,2) | 体重 (kg) | NOT NULL |
| body_fat | DECIMAL(4,2) | 体脂率 (%) | |
| goal | ENUM | 健身目标 (lose, gain, maintain) | NOT NULL |
| bio | TEXT | 个人简介/个性签名 | |
| updated_at | TIMESTAMP | 指标最后更新时间 | |

---

### 3.2 社交与追踪模块 (Social & Tracking)

#### 3.2.1 `user_follows` (粉丝与关注关系)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| follower_id | INT | 关注者用户 ID | PK, FK (users.id) |
| followed_id | INT | 被关注者用户 ID | PK, FK (users.id) |
| created_at | TIMESTAMP | 关注发生时间 | |

#### 3.2.2 `user_footprints` (用户浏览足迹)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 足迹记录唯一 ID | PK, AI |
| user_id | INT | 用户 ID | FK (users.id) |
| target_type | ENUM | 浏览对象类型 (POST, WORKOUT, COACH, DIET) | NOT NULL |
| target_id | VARCHAR(50) | 浏览对象 ID | NOT NULL |
| viewed_at | TIMESTAMP | 浏览发生的时间 | DEFAULT CURRENT_TIMESTAMP |

---

### 3.3 收藏模块 (Collections)

#### 3.3.1 `collection_folders` (收藏文件夹/集)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 文件夹唯一 ID | PK, AI |
| user_id | INT | 创建者 ID | FK (users.id) |
| name | VARCHAR(100) | 收藏夹名称 | NOT NULL |
| description | TEXT | 收藏夹描述 | |
| is_public | BOOLEAN | 是否公开可见 | DEFAULT FALSE |
| created_at | TIMESTAMP | 创建时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

#### 3.3.2 `user_collections` (收藏内容明细)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 收藏项 ID | PK, AI |
| folder_id | INT | 关联的收藏夹 ID | FK (collection_folders.id) |
| user_id | INT | 用户 ID（冗余字段用于加速权限检查） | FK (users.id) |
| target_type | ENUM | 收藏对象类型 (POST, WORKOUT, ARTICLE) | NOT NULL |
| target_id | VARCHAR(50) | 收藏对象 ID | NOT NULL |
| created_at | TIMESTAMP | 收藏时间 | |

---

### 3.4 训练模块 (Workouts & Sessions)

#### 3.4.1 `workouts` (训练项目配置)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | VARCHAR(50) | 训练项目唯一标识 (如 'squat') | PK |
| name | VARCHAR(100) | 项目中文名称 | NOT NULL |
| en_name | VARCHAR(100) | 项目英文名称 | NOT NULL |
| difficulty | INT | 推荐难度 (1-5 星) | |
| tags | JSON | 项目标签（如 ["臀腿", "力量"]） | |
| description | TEXT | 项目详细描述 | |
| theme_color | VARCHAR(20) | 前端 UI 渲染主题色 | |
| created_at | TIMESTAMP | 创建时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

#### 3.4.2 `workout_sessions` (AI 训练战报记录)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 战报唯一 ID | PK, AI |
| user_id | INT | 训练用户 ID | FK (users.id) |
| workout_id | VARCHAR(50) | 关联的训练项目 ID | FK (workouts.id) |
| score | INT | AI 综合评分 | NOT NULL |
| grade | VARCHAR(5) | 评级 (A, B, C...) | NOT NULL |
| grade_level | VARCHAR(20) | 评级样式类名 (grade-A...) | |
| comment | TEXT | AI 动作点评建议 | |
| valid_reps | INT | 有效动作次数 | |
| invalid_reps | INT | 异常动作次数 | |
| duration_minutes| INT | 训练时长 (分钟) | |
| calories_burned | INT | 消耗热量 (kcal) | |
| radar_scores | JSON | 五维姿态评分数组 | |
| snapshots | JSON | AI 纠错抓拍列表 (包含图片 URL 与错误原因) | |
| created_at | TIMESTAMP | 训练时间 | |

---

### 3.5 社区互动模块 (Community)

#### 3.5.1 `community_posts` (推文动态)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 动态唯一 ID | PK, AI |
| user_id | INT | 发布者 ID | FK (users.id) |
| content | TEXT | 动态正文内容 | NOT NULL |
| topic | VARCHAR(50) | 话题标签 (#话题名称) | |
| device | VARCHAR(50) | 发布设备名称 | |
| ai_report_id | INT | 关联的 AI 训练战报 ID | FK (workout_sessions.id) |
| created_at | TIMESTAMP | 发布时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

#### 3.5.2 `post_likes` (动态点赞关系)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| post_id | INT | 被点赞动态 ID | PK, FK (community_posts.id) |
| user_id | INT | 点赞用户 ID | PK, FK (users.id) |
| created_at | TIMESTAMP | 点赞时间 | |

#### 3.5.3 `post_comments` (动态评论/回复)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 评论唯一 ID | PK, AI |
| post_id | INT | 关联动态 ID | FK (community_posts.id) |
| user_id | INT | 评论用户 ID | FK (users.id) |
| parent_id | INT | 父级评论 ID（支持树状回复结构） | FK (post_comments.id) |
| content | TEXT | 评论内容 | NOT NULL |
| created_at | TIMESTAMP | 评论时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

---

### 3.6 饮食模块 (Diet)

#### 3.6.1 `meals` (每日饮食记录)
| 字段名 | 类型 | 描述 | 约束 |
| :--- | :--- | :--- | :--- |
| id | INT | 记录唯一 ID | PK, AI |
| user_id | INT | 用户 ID | FK (users.id) |
| meal_time | DATETIME | 进餐时间 | NOT NULL |
| meal_type | ENUM | 餐次类型 (breakfast, lunch, dinner, snack) | NOT NULL |
| food_name | TEXT | 食物名称（可存储多个） | NOT NULL |
| calories | INT | 摄入热量 (kcal) | |
| tag_type | VARCHAR(20) | UI 渲染标签样式 (info, success, warning) | |
| created_at | TIMESTAMP | 记录创建时间 | |
| deleted_at | TIMESTAMP | 软删除标记 | |

## 4. 核心关系映射 (ER)
1. **1:1** - `users` <-> `user_profiles`
2. **1:N** - `users` -> `community_posts`, `workout_sessions`, `meals`, `collection_folders`, `user_footprints`
3. **N:M** - `users` <-> `users` (via `user_follows`)
4. **N:M** - `users` <-> `community_posts` (via `post_likes`)
5. **树状关联** - `post_comments` (via `parent_id`)
6. **可选关联** - `community_posts` -> `workout_sessions` (动态可携带一份 AI 战报)
