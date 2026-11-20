# CourseShare API 说明

## 通用约定

- 基础路径：`/api`
- 请求和响应字符集：UTF-8
- 登录态：HttpOnly Session Cookie `CS_SESSION`
- 修改类请求可携带 `Origin`，服务端执行同源校验。
- 每次 API 响应包含 `X-Request-Id` 响应头。
- 统一响应：`code`、`message`、`data`、`requestId`。
- 204 响应无响应体。
- 时间格式：ISO 8601，例如 `2026-11-21T10:30:00`。

## 认证

### POST `/auth/register`

```json
{
  "username": "learner01",
  "password": "password123",
  "nickname": "学习者",
  "email": "learner@example.com",
  "enableAuthor": true
}
```

公开接口，返回 201。`enableAuthor` 为可选字段；注册时选择分享资源会同时开通创作功能。

### POST `/auth/author/enable`

需要登录。为当前账号开通创作功能，可重复调用，返回更新后的当前用户信息。

### POST `/auth/login`

```json
{
  "username": "admin",
  "password": "password"
}
```

返回当前用户、角色和权限。登录成功后轮换 Session。

### POST `/auth/logout`

需要登录，返回 204。

### GET `/auth/me`

需要登录，返回当前用户、角色和权限。

## 分类

### GET `/categories/tree`

公开返回启用分类树。

### GET `/admin/categories/tree`

权限：`category:manage`。包含停用分类和已发布资源数。

### POST `/admin/categories`

权限：`category:manage`。

```json
{
  "parentId": null,
  "name": "算法与数据结构",
  "description": "算法学习资料",
  "sortOrder": 50,
  "status": 1
}
```

### PUT `/admin/categories/{id}`

编辑名称、说明、排序、状态或移动父分类，必须携带当前 `version`。

### PATCH `/admin/categories/{id}/status`

```json
{
  "status": 0,
  "version": 3
}
```

停用分类会级联停用所有后代。

### DELETE `/admin/categories/{id}`

分类必须没有子分类和未删除资源，成功返回 204。

## 资源

### GET `/resources`

公开资源分页，参数：

| 参数 | 说明 |
|---|---|
| `keyword` | 标题或摘要模糊搜索 |
| `categoryId` | 分类，包含后代 |
| `authorId` | 作者筛选 |
| `page` | 默认 1 |
| `size` | 默认 10，最大 100 |
| `sortBy` | `publishedAt`、`viewCount`、`downloadCount` |
| `sortDirection` | `desc` 或 `asc` |

### GET `/resources/{id}`

公开资源详情，成功后原子增加浏览数。

### GET `/resources/{id}/download`

权限：`resource:download`。资源必须已发布且分类启用。

### GET `/admin/resources/{id}/download`

权限：`resource:audit:list`。审核员可下载任意未删除资源的附件，便于审核待发布内容。

## 作者资源

### GET `/author/resources`

查询当前作者资源，支持 `keyword`、`status`、`categoryId`、分页和排序。

### POST `/author/resources`

`multipart/form-data`：

- `data`：`application/json` 的 `ResourceSaveRequest`
- `file`：可选附件

```text
data={"title":"Spring 笔记","summary":"请求链路","content":"# 内容","categoryId":6}
```

### PUT `/author/resources/{id}`

格式同创建，`data` 必须携带 `version`。仅草稿、驳回和下架状态可编辑。

### POST `/author/resources/{id}/submit`

```json
{ "version": 2 }
```

### POST `/author/resources/{id}/withdraw`

仅待审核资源可撤回，body 为 `{ "version": 2 }`。

### POST `/author/resources/{id}/offline`

仅已发布资源可下架，body 为 `{ "version": 2 }`。

### DELETE `/author/resources/{id}`

仅草稿、驳回和下架状态可删除。

## 管理员资源审核

### GET `/admin/resources`

权限：`resource:audit:list`。在公开查询参数基础上增加 `status`、`dateFrom`、`dateTo`。

### GET `/admin/resources/{id}`

权限：`resource:audit:list`。返回审核详情。

### POST `/admin/resources/{id}/approve`

```json
{ "version": 1 }
```

### POST `/admin/resources/{id}/reject`

```json
{
  "version": 1,
  "remark": "请补充附件和章节说明"
}
```

### POST `/admin/resources/batch-publish`

```json
{
  "items": [
    { "id": 11, "version": 2 },
    { "id": 12, "version": 4 }
  ]
}
```

所有资源必须存在、待审核且版本一致，否则整批事务回滚。

### POST `/admin/resources/{id}/offline`

Body 为 `{ "version": 2 }`。

### DELETE `/admin/resources/{id}`

仅草稿、驳回和下架状态可删除，返回 204。

## 用户与角色

### GET `/admin/users`

参数：`keyword`、`status`、`page`、`size`。

### GET `/admin/roles`

返回启用角色选项。

### PUT `/admin/users/{id}/roles`

```json
{
  "roleCodes": ["USER", "AUTHOR"]
}
```

不能移除当前账号或系统中最后一个管理员。

### PATCH `/admin/users/{id}/status`

```json
{ "status": 0 }
```

不能停用当前登录账号或最后一个管理员。

## 日志

### GET `/admin/logs`

权限：`log:view`。参数：`keyword`、`module`、`userId`、`success`、`dateFrom`、`dateTo`、`page`、`size`。

## 收藏

### PUT `/resources/{id}/favorite`

权限：登录用户。幂等收藏已发布资源。

### DELETE `/resources/{id}/favorite`

权限：登录用户。幂等取消收藏。

### GET `/me/favorites`

权限：登录用户。参数：`page`、`size`。只返回仍处于发布状态且分类启用的资源。

### GET `/me/favorite-ids`

权限：登录用户。返回当前账号收藏的资源 ID 数组。

## 评价与点赞

### GET `/resources/{id}/reviews`

公开分页列表，参数：`page`、`size`。返回 `reviews` 分页结果、`averageScore`、`ratingCount` 和当前登录用户的 `mine`。管理员查看时同时返回隐藏评价，普通用户只看到公开评价。

### PUT `/resources/{id}/review`

权限：登录用户。

```json
{
  "score": 5,
  "content": "章节清晰，示例完整。"
}
```

`score` 必填且为 1–5；`content` 可选，最多 500 字。同一用户再次提交会更新原评价。作者不能评价自己的资源。

### DELETE `/resources/{id}/review`

权限：登录用户。删除自己的评价及其点赞。

### PUT `/reviews/{id}/like`

权限：登录用户。幂等点赞；可以点赞自己的评价，隐藏评价不可点赞。

### DELETE `/reviews/{id}/like`

权限：登录用户。幂等取消点赞。

### GET `/me/reviews`

权限：登录用户。分页返回自己的全部评价，包括隐藏评价和资源当前状态。

### PATCH `/admin/reviews/{id}/visibility`

权限：`review:moderate`。

```json
{ "visible": false }
```

## 发现

### GET `/discover/resources`

公开接口。参数：`sort`、`page`、`size`。

| sort | 排序方式 |
|---|---|
| `latest` | 发布时间倒序 |
| `popular` | 综合浏览、下载、收藏、评分人数和评价点赞数 |
| `rating` | 公开评价平均分倒序，仅返回至少一条评价的资源 |
| `likes` | 公开评价点赞总数倒序，仅返回至少一个点赞的资源 |

返回 `DiscoverResourceVO`，包含平均分、评分人数、收藏数、评价获赞数和综合热度。

## 错误码

| HTTP | code | 场景 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 参数或状态字段不合法 |
| 400 | `FILE_REJECTED` | 附件类型、大小或路径不合法 |
| 401 | `UNAUTHORIZED` | 未登录或登录失效 |
| 403 | `FORBIDDEN` | 无权限、归属不符或账号停用 |
| 404 | `NOT_FOUND` | 用户、分类、资源或附件不存在 |
| 409 | `STATE_CONFLICT` | 当前状态不允许操作 |
| 409 | `VERSION_CONFLICT` | 数据已被其他操作修改 |
| 409 | `DATA_CONFLICT` | 唯一键或引用约束冲突 |
| 500 | `INTERNAL_ERROR` | 未预期系统异常 |

