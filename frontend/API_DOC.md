# AiTrainer 后端接口文档 (v2.0)

## 1. 基础配置
- **Base URL**: `http://localhost:3000/api`
- **Authentication**: `Bearer {jwt_token}`

## 2. 用户与社交 (Auth & Social)

### 2.1 粉丝与关注
- **`GET /social/followers`**: 获取粉丝列表。
- **`GET /social/following`**: 获取关注列表。
- **`POST /social/follow/:userId`**: 关注/取消关注某人。

### 2.2 浏览足迹
- **`GET /user/footprints`**: 获取用户最近的浏览足迹。
- **`POST /user/footprints`**: 记录一次浏览行为。
  - Body: `{ "targetType": "POST|WORKOUT", "targetId": "string" }`

## 3. 收藏管理 (Collections)

### 3.1 收藏夹操作
- **`GET /collections/folders`**: 获取所有收藏夹。
- **`POST /collections/folders`**: 新建收藏夹。
  - Body: `{ "name": "string", "description": "string", "isPublic": boolean }`
- **`DELETE /collections/folders/:id`**: 删除收藏夹（软删除）。

### 3.2 收藏项操作
- **`POST /collections/items`**: 将实体加入收藏夹。
  - Body: `{ "folderId": number, "targetType": "string", "targetId": "string" }`
- **`DELETE /collections/items/:id`**: 移除收藏项。

## 4. 社区交互 (Post Interactions)

### 4.1 点赞与取消点赞
- **`POST /posts/:id/like`**: 点赞/取消点赞。

### 4.2 评论管理
- **`GET /posts/:id/comments`**: 获取某篇推文的所有评论（支持树状结构）。
- **`POST /posts/:id/comments`**: 发表评论或回复他人。
  - Body: `{ "content": "string", "parentId": number | null }`
- **`DELETE /comments/:id`**: 删除自己的评论（软删除）。

## 5. 饮食管理 (Diet)
- **`GET /diet/meals`**: 获取今日饮食记录。
- **`POST /diet/meals`**: 添加饮食记录。
- **`DELETE /diet/meals/:id`**: 删除饮食记录（软删除）。

## 6. 训练战报 (Workouts)
- **`GET /workouts/sessions`**: 获取历史训练列表。
- **`GET /workouts/sessions/:id`**: 获取详细战报数据。
- **`POST /workouts/sessions`**: 提交 AI 训练结果。
