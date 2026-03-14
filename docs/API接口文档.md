# API 接口文档

> 版本：v1.0 | 更新日期：2026-03-15  
> 基础路径：`http://localhost:8080`  
> 认证方式：`Authorization: Bearer <JWT Token>`

---

## 通用规范

### 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```
- `code`: 200 成功，401 未认证，403 无权限，500 业务异常
- `message`: 操作结果描述
- `data`: 返回数据（可为 null）

### 分页参数
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pageNum` | int | 1 | 当前页码 |
| `pageSize` | int | 10 | 每页条数 |

### 分页响应
```json
{
  "records": [],
  "total": 100,
  "pages": 10,
  "current": 1,
  "size": 10
}
```

---

## 1. 认证模块 `UserController`

### 1.1 用户注册
- **URL**: `POST /api/auth/register`
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "2021001@edu.cn",
  "password": "123456"
}
```
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "2021001@edu.cn",
    "role": "STUDENT",
    "status": 1,
    "password": null
  }
}
```

### 1.2 用户登录
- **URL**: `POST /api/auth/login`
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "2021001@edu.cn",
  "password": "123456"
}
```
- **成功响应**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzUxMi..."
  }
}
```

### 1.3 获取咨询师列表
- **URL**: `GET /api/auth/teachers`
- **认证**: 不需要
- **成功响应**: 返回角色为 TEACHER 的用户列表（密码已脱敏为 null）

---

## 2. 科普文章模块 `ArticleController`

### 2.1 文章列表（分页+搜索）
- **URL**: `GET /api/article/list`
- **认证**: 不需要
- **参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNum` | int | 否 | 页码，默认 1 |
| `pageSize` | int | 否 | 每页条数，默认 10 |
| `keyword` | string | 否 | 搜索关键词（匹配标题） |

### 2.2 文章详情
- **URL**: `GET /api/article/{id}`
- **认证**: 不需要
- **说明**: 访问即自动 +1 阅读量

---

## 3. 树洞模块 `PostController`

### 3.1 发布树洞
- **URL**: `POST /api/post/publish`
- **认证**: ✅ 需要
- **请求体**:
```json
{
  "content": "今天心情不太好...",
  "tags": "压力,焦虑",
  "isAnonymous": 1,
  "images": "/uploads/xxx.jpg"
}
```
- **说明**: 内容自动经过 DFA 敏感词过滤

### 3.2 树洞广场（分页）
- **URL**: `GET /api/post/square`
- **认证**: 不需要
- **参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNum` | int | 否 | 页码 |
| `pageSize` | int | 否 | 每页条数 |
| `sortBy` | string | 否 | 排序方式：`latest`(默认) / `hot` |
| `tags` | string | 否 | 标签过滤 |

### 3.3 树洞详情
- **URL**: `GET /api/post/{id}`
- **认证**: 不需要
- **说明**: 访问自动 +1 浏览量

### 3.4 点赞树洞
- **URL**: `POST /api/post/{id}/like`
- **认证**: ✅ 需要

---

## 4. 评论模块 `CommentController`

### 4.1 发布评论
- **URL**: `POST /api/comment/publish`
- **认证**: ✅ 需要
- **请求体**:
```json
{
  "postId": 1,
  "content": "加油！一切都会好的",
  "isAnonymous": 1,
  "parentId": null
}
```

### 4.2 获取评论列表
- **URL**: `GET /api/comment/list/{postId}`
- **认证**: 不需要
- **说明**: 按时间正序排列

### 4.3 点赞评论
- **URL**: `POST /api/comment/{id}/like`
- **认证**: ✅ 需要

---

## 5. 预约模块 `AppointmentController`

### 5.1 学生端 - 提交预约
- **URL**: `POST /api/appointment/submit`
- **认证**: ✅ 需要（学生角色）
- **请求体**:
```json
{
  "teacherId": 2,
  "appointDate": "2026-03-20",
  "timeSlot": "09:00-10:00",
  "requirement": "最近压力很大，想聊聊"
}
```
- **说明**: 自动进行时段冲突检测

### 5.2 学生端 - 我的预约
- **URL**: `GET /api/appointment/my`
- **认证**: ✅ 需要

### 5.3 教师端 - 预约列表
- **URL**: `GET /api/appointment/teacher/list`
- **认证**: ✅ 需要（教师角色）
- **参数**: `status`（可选，过滤状态：PENDING/APPROVED/REJECTED/COMPLETED）

### 5.4 教师端 - 审批预约
- **URL**: `POST /api/appointment/teacher/process`
- **认证**: ✅ 需要（教师角色）
- **参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `appointmentId` | Long | 是 | 预约记录 ID |
| `status` | string | 是 | APPROVED / REJECTED / COMPLETED |
| `feedback` | string | 否 | 反馈理由 |

---

## 6. 测评模块 `QuizController`

### 6.1 问卷列表
- **URL**: `GET /api/quiz/list`
- **认证**: 不需要
- **参数**: `pageNum`, `pageSize`

### 6.2 获取题目
- **URL**: `GET /api/quiz/{id}/questions`
- **认证**: 不需要
- **说明**: 返回按 `sortOrder` 排序的题目列表

### 6.3 提交答卷
- **URL**: `POST /api/quiz/{id}/submit`
- **认证**: ✅ 需要
- **请求体**:
```json
{
  "1": 3,
  "2": 5,
  "3": 2
}
```
- **格式**: `Map<questionId, score>`，key 为题目 ID，value 为选择分值

### 6.4 我的测评历史
- **URL**: `GET /api/quiz/my-results`
- **认证**: ✅ 需要

---

## 7. 排班模块 `TeacherScheduleController`

### 7.1 教师端 - 发布/更新排班
- **URL**: `POST /api/schedule/publish`
- **认证**: ✅ 需要（教师角色）
- **请求体**:
```json
{
  "workDate": "2026-03-20",
  "availableSlots": "09:00-10:00,10:30-11:30,14:00-15:00"
}
```

### 7.2 教师端 - 我的排班
- **URL**: `GET /api/schedule/my`
- **认证**: ✅ 需要（教师角色）
- **参数**: `startDate`, `endDate`

### 7.3 学生端 - 查询教师排班
- **URL**: `GET /api/schedule/teacher/{teacherId}`
- **认证**: ✅ 需要
- **参数**: `startDate`, `endDate`

---

## 8. 聊天模块 `ChatController`

### 8.1 获取历史消息
- **URL**: `GET /api/chat/history`
- **认证**: ✅ 需要
- **参数**: `targetId`（对方用户 ID）
- **说明**: 返回双方之间的全部聊天记录，按时间正序

### 8.2 WebSocket 连接
- **URL**: `ws://localhost:8080/ws/chat/{userId}`
- **说明**: 建立连接后发送 JSON 消息
- **消息格式**:
```json
{
  "receiverId": 2,
  "content": "你好，我想咨询一下...",
  "type": "TEXT"
}
```

---

## 9. 管理模块 `AdminController`

> ⚠️ 以下接口均需要 `ROLE_ADMIN` 权限

### 9.1 用户状态变更
- **URL**: `POST /api/admin/user/{id}/status/{status}`
- **参数**: `status` = 0（封禁）/ 1（正常）

### 9.2 文章保存/更新
- **URL**: `POST /api/admin/article/save`
- **请求体**: Article 对象（有 id 则更新，无 id 则新增）

### 9.3 文章状态变更
- **URL**: `POST /api/admin/article/{id}/status/{status}`
- **参数**: `status` = 0（下架）/ 1（发布）

### 9.4 问卷保存/更新
- **URL**: `POST /api/admin/quiz/save`
- **请求体**: Quiz 对象

### 9.5 问卷状态变更
- **URL**: `POST /api/admin/quiz/{id}/status/{status}`
- **参数**: `status` = 0（下线）/ 1（上线）

---

## 10. 公共模块 `CommonController`

### 10.1 文件上传
- **URL**: `POST /api/common/upload`
- **认证**: ✅ 需要
- **参数**: `file`（MultipartFile，最大 10MB）
- **成功响应**:
```json
{
  "code": 200,
  "data": "/uploads/a1b2c3d4-uuid.jpg"
}
```

---

## 附录：状态码对照表

| 状态码 | 含义 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数校验失败（MethodArgumentNotValidException） |
| 401 | 未认证 / Token 过期 |
| 403 | 无权限（角色不匹配） |
| 500 | 业务异常 / 系统异常 |
