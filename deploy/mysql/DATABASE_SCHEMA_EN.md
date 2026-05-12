# AiTrainer Database Schema

> Generated from the init script: `deploy/mysql/init/init.sql`  
> Database: `ai_trainer`  
> Charset: `utf8mb4`

## 1. Overview

This database supports AiTrainer’s core features, including user accounts, workout programs and reports, community interactions (posts/comments/likes/favorites), AI coach chat history, and diet logs.

### 1.1 Conventions

- Most business tables include soft-delete fields (e.g. `is_deleted`, `deleted_at`). Make sure queries filter them appropriately.
- Timestamp fields are typically `created_at / updated_at` or `create_time / update_time`, used for auditing and sorting.

## 2. Table List

| No. | Table | Description |
| --- | --- | --- |
| 1 | users | Users (core) |
| 2 | workouts | Workout program definitions |
| 3 | extra_exercises | Extra exercises |
| 4 | user_profiles | User profile |
| 5 | user_privacy_settings | User privacy settings |
| 6 | ai_coach_chat_history | AI coach chat history |
| 7 | collection_folder | Collection folders |
| 8 | community_guestbook | User space guestbook |
| 9 | workout_session_likes | Workout report likes |
| 10 | meals | Meals / diet logs |
| 11 | user_follows | User follows |
| 12 | workout_sessions | Workout sessions (reports) |
| 13 | collection_item | Collection items |
| 14 | community_posts | Community posts |
| 15 | post_images | Post images |
| 16 | post_likes | Post likes |
| 17 | post_favorites | Post favorites |
| 18 | post_comments | Post comments (self-referencing) |

## 3. Schema Details

### 3.1 users

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| username | VARCHAR(50) | No | - | Login username |
| email | VARCHAR(100) | No | - | Email address |
| password_hash | VARCHAR(255) | No | - | Password hash |
| avatar | VARCHAR(255) | Yes | NULL | Avatar URL |
| is_pro | TINYINT(1) | Yes | 0 | PRO membership |
| is_first_login | TINYINT(1) | Yes | 1 | First-login flag |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |
| updated_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Updated time (auto) |
| deleted_at | TIMESTAMP | Yes | NULL | Deleted time |
| follower_count | INT | Yes | NULL | Follower count |
| following_count | INT | Yes | NULL | Following count |
| status | TINYINT | Yes | 0 | Status: 0-normal, -1-deactivated |
| token_version | INT | Yes | 0 | Token version |

**Constraints & Indexes**

- Unique: `email`, `username`
- Indexes: `idx_email(email)`, `idx_username(username)`

---

### 3.2 workouts

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | VARCHAR(50) | No | - | Program id (PK) |
| name | VARCHAR(100) | No | - | Chinese name |
| en_name | VARCHAR(100) | No | - | English name |
| difficulty | INT | Yes | 1 | Recommended difficulty |
| tags | JSON | Yes | NULL | Tags |
| description | TEXT | Yes | NULL | Description |
| theme_color | VARCHAR(20) | Yes | #409EFF | Theme color |
| cover_url | VARCHAR(255) | Yes | NULL | Cover image URL |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |
| is_deleted | INT | Yes | 0 | Soft delete (0-normal, 1-deleted) |

---

### 3.3 extra_exercises

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| user_id | INT | No | - | User id |
| exercise_name | VARCHAR(100) | No | - | Exercise name |
| description | VARCHAR(500) | Yes | NULL | Notes (pace/distance/etc.) |
| calories_burned | INT | Yes | 0 | Calories burned (kcal) |
| duration_minutes | INT | Yes | 0 | Duration (minutes) |
| exercise_date | DATE | No | - | Exercise date |
| is_deleted | TINYINT | Yes | 0 | Soft delete (0-normal, 1-deleted) |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Indexes**

- `idx_user_exercise_date(user_id, exercise_date)`

---

### 3.4 user_profiles

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| user_id | BIGINT | No | - | User id, references users (PK) |
| nickname | VARCHAR(50) | No | - | Nickname |
| gender | VARCHAR(10) | No | - | Gender |
| goal | VARCHAR(20) | No | - | Fitness goal |
| bio | VARCHAR(255) | Yes | NULL | Bio |
| height | INT | Yes | NULL | Height (cm) |
| weight | DECIMAL(5,2) | Yes | NULL | Weight (kg) |
| body_fat | DECIMAL(4,2) | Yes | NULL | Body fat (%) |
| updated_at | DATETIME | No | CURRENT_TIMESTAMP | Updated time (auto) |
| total_likes | INT UNSIGNED | Yes | 0 | Total likes received |
| age | INT | Yes | NULL | Age |

---

### 3.5 user_privacy_settings

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| user_id | BIGINT | No | - | User id (PK) |
| public_ai_report | TINYINT(1) | No | 1 | Public AI reports (1-public, 0-private) |
| updated_at | DATETIME | No | CURRENT_TIMESTAMP | Updated time (auto) |

---

### 3.6 ai_coach_chat_history

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| user_id | BIGINT | No | - | User id |
| session_id | VARCHAR(100) | No | - | Session id |
| role | VARCHAR(20) | No | - | Role: user / assistant |
| content | TEXT | No | - | Message content |
| analysis_type | VARCHAR(50) | Yes | NULL | training/diet/comprehensive/chat |
| created_at | DATETIME | No | CURRENT_TIMESTAMP | Created time |
| reply_to | BIGINT | Yes | NULL | Related question message id |

**Indexes**

- `idx_created_at(created_at)`
- `idx_reply_to(reply_to)`
- `idx_session_id(session_id)`
- `idx_user_id(user_id)`
- `idx_user_session(user_id, session_id)`

---

### 3.7 collection_folder

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| user_id | BIGINT | No | - | Owner user id |
| name | VARCHAR(100) | No | - | Folder name |
| is_public | TINYINT | Yes | 0 | 0-private, 1-public |
| is_default | TINYINT | Yes | 0 | 0-normal, 1-default |
| create_time | DATETIME | Yes | CURRENT_TIMESTAMP | Created time |
| update_time | DATETIME | Yes | CURRENT_TIMESTAMP | Updated time (auto) |
| is_deleted | TINYINT | Yes | 0 | Soft delete |

**Indexes**

- `idx_user_id(user_id)`

---

### 3.8 community_guestbook

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| from_user_id | BIGINT | No | - | Author user id |
| to_user_id | BIGINT | No | - | Target user id |
| content | VARCHAR(500) | No | - | Message content |
| create_time | DATETIME | No | CURRENT_TIMESTAMP | Created time |
| reply_content | VARCHAR(500) | Yes | NULL | Reply content |
| reply_time | DATETIME | Yes | NULL | Reply time |
| is_deleted | TINYINT(1) | No | 0 | Soft delete |
| update_time | DATETIME | No | CURRENT_TIMESTAMP | Updated time (auto) |

**Indexes**

- `idx_from_user(from_user_id)`
- `idx_to_user(to_user_id)`

---

### 3.9 workout_session_likes

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| session_id | BIGINT | No | - | Workout session id |
| user_id | BIGINT | No | - | Liked by user id |
| created_at | DATETIME | Yes | CURRENT_TIMESTAMP | Created time |

**Constraints**

- Unique: `uk_session_user(session_id, user_id)` (prevent duplicate likes)

---

### 3.10 meals

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| user_id | INT | No | - | User id |
| meal_time | DATETIME | No | - | Meal time |
| meal_type | ENUM('breakfast','lunch','dinner','snack') | No | - | Meal type |
| food_name | TEXT | No | - | Food name |
| calories | INT | Yes | 0 | Calories |
| tag_type | VARCHAR(20) | Yes | info | Tag style |
| weight | INT | Yes | 0 | Weight (g) |
| protein | DECIMAL(10,2) | Yes | 0.00 | Protein (g) |
| fat | DECIMAL(10,2) | Yes | 0.00 | Fat (g) |
| carbs | DECIMAL(10,2) | Yes | 0.00 | Carbs (g) |
| is_deleted | TINYINT | Yes | 0 | Soft delete |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Foreign Keys**

- `meals.user_id` → `users.id` (ON DELETE CASCADE)

**Indexes**

- `idx_user_meals(user_id, meal_time)`

---

### 3.11 user_follows

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| follower_id | INT | No | - | Follower id |
| followed_id | INT | No | - | Followed id |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Constraints & Foreign Keys**

- Unique: `uk_follower_followed(follower_id, followed_id)`
- `follower_id` → `users.id` (ON DELETE CASCADE)
- `followed_id` → `users.id` (ON DELETE CASCADE)

**Indexes**

- `idx_followed(followed_id)`
- `idx_follower(follower_id)`

---

### 3.12 workout_sessions

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| user_id | INT | No | - | User id |
| workout_id | VARCHAR(50) | No | - | Workout program id |
| score | INT | No | - | Overall score |
| grade | VARCHAR(5) | No | - | Grade |
| grade_level | VARCHAR(20) | Yes | NULL | Grade CSS class |
| comment | TEXT | Yes | NULL | AI comment |
| valid_reps | INT | Yes | 0 | Valid reps |
| invalid_reps | INT | Yes | 0 | Invalid reps |
| duration_seconds | INT | Yes | 0 | Duration (seconds) |
| calories_burned | INT | Yes | 0 | Calories burned |
| like_count | INT | Yes | 0 | Total likes |
| radar_scores | JSON | Yes | NULL | Radar scores |
| snapshots | JSON | Yes | NULL | Correction snapshots |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |
| is_deleted | TINYINT | Yes | 0 | Soft delete |

**Foreign Keys**

- `user_id` → `users.id` (ON DELETE CASCADE)
- `workout_id` → `workouts.id`

**Indexes**

- `idx_user_workouts(user_id, created_at)`
- `workout_id(workout_id)`

---

### 3.13 collection_item

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| folder_id | BIGINT | No | - | Folder id |
| post_id | BIGINT | No | - | Post id |
| user_id | BIGINT | No | - | Owner user id (redundant) |
| create_time | DATETIME | Yes | CURRENT_TIMESTAMP | Created time |
| is_deleted | TINYINT | Yes | 0 | Soft delete |

**Constraints & Indexes**

- Unique: `uk_folder_post(folder_id, post_id)`
- Indexes: `idx_folder_id(folder_id)`, `idx_user_post(user_id, post_id)`

---

### 3.14 community_posts

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| user_id | INT | No | - | Author user id |
| content | TEXT | No | - | Content |
| topic | VARCHAR(50) | Yes | NULL | Topic |
| device | VARCHAR(50) | Yes | AiTrainer App | Device |
| ai_report_id | INT | Yes | NULL | Related workout session id |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |
| updated_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Updated time (auto) |
| deleted_at | TIMESTAMP | Yes | NULL | Deleted time |
| like_count | INT | Yes | 0 | Like count |
| favorite_count | INT | Yes | 0 | Favorite count |
| comment_count | INT | Yes | 0 | Comment count |
| is_deleted | TINYINT | Yes | 0 | Soft delete |

**Foreign Keys**

- `user_id` → `users.id` (ON DELETE CASCADE)
- `ai_report_id` → `workout_sessions.id` (ON DELETE SET NULL)

**Indexes**

- `idx_ai_report_id(ai_report_id)`
- `idx_post_created_at(created_at)`
- `idx_topic_created(topic, created_at)`
- `idx_user_posts(user_id, created_at)`

---

### 3.15 post_images

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | BIGINT | No | auto increment | Primary key |
| post_id | INT | No | - | Post id |
| object_key | VARCHAR(255) | No | - | OSS object key |
| sort_order | INT | Yes | 0 | Sort order |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Foreign Keys**

- `post_id` → `community_posts.id` (ON DELETE CASCADE)

**Indexes**

- `idx_post_images(post_id, sort_order)`

---

### 3.16 post_likes

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| post_id | INT | No | - | Post id (composite PK) |
| user_id | INT | No | - | User id (composite PK) |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Primary Key & Foreign Keys**

- PK: `(post_id, user_id)`
- `post_id` → `community_posts.id` (ON DELETE CASCADE)
- `user_id` → `users.id` (ON DELETE CASCADE)

**Indexes**

- `idx_like_user_id(user_id)`

---

### 3.17 post_favorites

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| post_id | INT | No | - | Post id (composite PK) |
| user_id | INT | No | - | User id (composite PK) |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |

**Primary Key & Foreign Keys**

- PK: `(post_id, user_id)`
- `post_id` → `community_posts.id` (ON DELETE CASCADE)
- `user_id` → `users.id` (ON DELETE CASCADE)

---

### 3.18 post_comments

**Columns**

| Column | Type | Nullable | Default | Notes |
| --- | --- | --- | --- | --- |
| id | INT | No | auto increment | Primary key |
| post_id | INT | No | - | Post id |
| user_id | INT | No | - | User id |
| parent_id | INT | Yes | NULL | Parent comment id |
| content | TEXT | No | - | Content |
| created_at | TIMESTAMP | Yes | CURRENT_TIMESTAMP | Created time |
| deleted_at | TIMESTAMP | Yes | NULL | Deleted time |
| is_deleted | TINYINT | Yes | 0 | Soft delete |

**Foreign Keys**

- `post_id` → `community_posts.id` (ON DELETE CASCADE)
- `user_id` → `users.id` (ON DELETE CASCADE)
- `parent_id` → `post_comments.id` (ON DELETE CASCADE, self-reference)

**Indexes**

- `idx_post_comments(post_id, created_at)`
- `idx_comment_parent_id(parent_id)`
- `idx_comment_user_id(user_id)`

## 4. Foreign Key Summary

| Child.column | Parent.column | On delete |
| --- | --- | --- |
| meals.user_id | users.id | CASCADE |
| user_follows.follower_id | users.id | CASCADE |
| user_follows.followed_id | users.id | CASCADE |
| workout_sessions.user_id | users.id | CASCADE |
| workout_sessions.workout_id | workouts.id | RESTRICT/default |
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

## 5. Seed Data

The init script inserts 3 example rows into `workouts`:

- `good_morning` (Good Morning)
- `lunge` (AI Lunge)
- `squat` (AI Squat)
