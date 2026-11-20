# 构建与联调验证记录

验证日期：2026-11-21

## 后端

```text
mvn -q test
Tests run: 17, Failures: 0, Errors: 0
```

覆盖：

- 草稿提交审核。
- 非作者操作被拒绝。
- 过期版本冲突。
- 审核通过写入发布字段。
- 分类第四级限制。
- 停用父分类下禁止创建启用子分类。
- MockMvc 统一响应。
- 收藏幂等写入。
- 作者禁止自评、用户可以自赞。
- 重复点赞不会重复累加计数。
- 无评价时评分摘要返回默认值。
- 发现页非法排序参数回退到综合热门。
- 交互 Mapper XML 与发现页聚合语句可被 MyBatis 正确解析。

```text
mvn -q package
BUILD SUCCESS
```

生成 `backend/target/course-share.war`。

## 前端

```text
npm run build
vue-tsc --noEmit
vite build
```

构建成功。Element Plus 采用按需组件加载。

```text
npm audit --audit-level=moderate
found 0 vulnerabilities
```

## Tomcat 部署

使用临时 Tomcat 10.1.62 部署最终 WAR：

| 请求 | 结果 |
|---|---|
| `/course-share/` | 200 |
| `/course-share/index.html` | 200 |
| `/course-share/resources/1` | 200 |
| `/course-share/admin/logs` | 200 |
| `/course-share/favicon.svg` | 200 |

Vue history 模式的深链接回退正常。

最终生产构建的静态资源、API 基路径和 Router 基路径均为 `/course-share/`，与 WAR 上下文一致。

## 隔离数据库联调

使用临时 MySQL 8.0.45 导入 `schema.sql` 和 `data.sql`：

- 4 个用户。
- 4 个角色。
- 16 项权限。
- 11 张业务与权限数据表。
- 2 条示例资源。

真实 API 链路结果：

| 步骤 | 结果 |
|---|---|
| 作者登录 | 200，角色 AUTHOR |
| 草稿提交审核 | 200 |
| 管理员登录 | 200，角色 ADMIN |
| 审核通过 | 200 |
| 公开详情 | 状态 2，已发布 |
| 数据库复核 | `status=2, audit_by=1` |
| 操作日志 | 成功写入 |
| Markdown 附件上传 | 成功 |
| 登录下载 | 5,155 字节 |
| 下载计数 | 1 |
| 收藏写入与幂等取消 | 服务测试与临时库约束验证通过 |
| 评价创建、修改、隐藏与恢复 | 服务测试与 Mapper 解析通过 |
| 发现页聚合与隐藏评价排除 | 临时库 SQL 验证通过 |

验证结束后已关闭 Tomcat 和 MySQL，并删除系统临时目录中的隔离数据。

## 未在当前机器执行

本机未对现有业务数据库执行覆盖式初始化；建表、种子数据、增量升级和聚合查询均在独立临时 MySQL 8 数据库中验证。生产部署时仍需要按 `docs/deployment.md` 设置实际数据库账号与附件目录。

