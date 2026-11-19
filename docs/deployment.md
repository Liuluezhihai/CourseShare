# 部署说明

## 环境

- JDK 17
- Maven 3.9+
- Node.js 20+
- MySQL 8
- Tomcat 10.1

## 数据库

```sql
SOURCE sql/schema.sql;
SOURCE sql/data.sql;
```

已有数据库升级时执行：

```sql
SOURCE sql/migration-interactions.sql;
```

初始化账号密码均为 `password`：

| 账号 | 角色 |
|---|---|
| admin | ADMIN |
| auditor | AUDITOR |
| author | AUTHOR |
| student | USER |

## 后端配置

复制：

```text
backend/src/main/resources/application-local.properties.example
→ backend/src/main/resources/application-local.properties
```

修改数据库口令、附件目录和安全 Cookie。外部附件目录不得位于 Tomcat Web 应用目录中。

## 开发启动

在 IDEA 中配置 Tomcat 10.1，部署 `backend/target/course-share` 或 WAR，上下文路径设置为 `/course-share`。

```powershell
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。

## 生产打包

```powershell
.\scripts\build.ps1
```

脚本依次：

1. 安装前端依赖并构建。
2. Maven 生成 WAR。
3. 使用 JDK `jar` 将前端产物更新进 WAR。

输出：

```text
backend/target/course-share.war
```

将 WAR 放入 Tomcat `webapps`，启动后访问：

```text
http://localhost:8080/course-share/
```

前端生产构建默认基路径为 `/course-share/`，与 WAR 上下文一致。如果修改 WAR 名称或上下文路径，请在构建前同步修改 `frontend/.env.production` 中的 `VITE_BASE_PATH`。

## 外置配置

生产环境应通过 JVM 系统属性覆盖敏感配置：

```text
-Djdbc.password=实际数据库口令
-Dstorage.root=D:/CourseShareData/uploads
-Dcourseshare.secure-cookie=true
```

Spring 的属性占位符可使用 JVM 系统属性覆盖默认值。使用 HTTPS 时必须开启 secure cookie。

## 检查清单

- 数据库脚本无报错，十一张表存在。
- 附件目录可写，且不在 Web 根目录下。
- `/api/categories/tree` 返回 200。
- admin 可以登录并看到完整后台。
- author 可以创建、编辑和提交资源。
- auditor 可以审核、驳回、批量发布和下架。
- 门户只显示已发布且分类启用的资源。
- 下载要求登录，下载后计数增加。
- 收藏、评价和评价点赞要求登录，未登录用户仍可浏览评价。
- 发现页的最新、综合热门、高分和评价获赞榜均可分页访问。
- 个人资料页“我的评价”只显示当前用户的数据。
- 浏览历史刷新后保留，退出登录不会清除。
- `/api/admin/logs` 中存在成功和失败操作记录。
- 前端刷新深层路由不会 404。

