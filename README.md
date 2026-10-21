# CourseShare 学习资源分享平台

基于传统 Spring Framework 6 + Spring MVC + MyBatis-Plus 的 WAR 项目，配套 Vue 3 管理门户。项目实现资源发布、审核、上下架、分类、RBAC、检索、下载、收藏、浏览历史、评价点赞、发现榜单、操作日志和 MyBatis-Plus 高级特性。

## 目录

```text
CourseShare
├── backend                 Maven WAR 后端
├── frontend                Vue 3 + TypeScript 前端
├── sql                     建表与初始化脚本
└── docs                    详细设计与部署说明
```

> 仓库根目录原有的 Jersey 实验骨架保持原样，CourseShare 正式实现位于 `backend` 与 `frontend`。

## 本地启动

1. 安装 Java 17、Maven 3.9、Node.js 20、MySQL 8 和 Tomcat 10.1。
2. 在 MySQL 中执行：

   ```sql
   SOURCE sql/schema.sql;
   SOURCE sql/data.sql;
   ```

3. 将 `backend/src/main/resources/application-local.properties.example` 复制为同目录下的 `application-local.properties`，填写数据库和附件目录。
4. 启动后端开发环境或执行 `mvn package` 生成 WAR，然后部署到 Tomcat 10.1。
5. 进入 `frontend`，执行 `npm install` 和 `npm run dev`，访问 Vite 输出的地址。

初始化账号的密码统一为 `password`：

| 账号 | 角色 |
|---|---|
| `admin` | 管理员 |
| `auditor` | 审核员 |
| `author` | 资源作者 |
| `student` | 普通用户 |

建议首次登录后立即修改密码，或者使用 `PasswordHashGenerator` 生成新的 BCrypt 密文。

## 文档

- [详细设计](docs/detailed-design.md)
- [API 说明](docs/api.md)
- [OpenAPI 契约](docs/openapi.yaml)
- [部署说明](docs/deployment.md)
- [构建与联调验证记录](docs/verification.md)
- [AI 使用记录模板](docs/ai-log-template.md)

## 构建

```powershell
.\scripts\build.ps1
```

构建后的 `course-share.war` 位于 `backend/target`，脚本会自动将前端 `dist` 更新进 WAR。Vite 开发服务器已经将 `/api` 代理到 `http://localhost:8080/course-share`。

## 配置

后端读取 `classpath:application.properties`，并允许外部属性覆盖。关键配置：

```properties
jdbc.url=jdbc:mysql://localhost:3306/courseshare?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=root
storage.root=./data/uploads
upload.max-size=52428800
```

附件目录必须位于 Tomcat Web 根目录之外。生产环境请通过 JVM 系统属性或环境变量覆盖数据库口令，不要提交真实凭据。

## 验收重点

- 用户、角色、权限、分类、资源、收藏、评价、点赞和日志十一表关系完整。
- 资源状态采用专用接口，包含归属校验、乐观锁和批量发布事务。
- 分类最多三级，支持循环检测、级联停用和删除保护。
- 门户资源查询使用一条 count 与一条联表数据查询，避免 N+1。
- 上传扩展名白名单、路径规范化、安全下载、日志脱敏和统一异常。
- 前端提供资源门户、创作中心、控制台、审核、分类、用户、日志及错误状态页面。
- 收藏按账号持久化，浏览历史保存在当前浏览器，发现页提供热度、评分和评价点赞榜单。
- 当前已有数据库升级时执行 `sql/migration-interactions.sql`。

