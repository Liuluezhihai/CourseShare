USE courseshare;

INSERT INTO sys_role (id, code, name, description, status) VALUES
    (1, 'USER', '注册用户', '浏览和下载已发布资源', 1),
    (2, 'AUTHOR', '资源作者', '创建和维护自己的学习资源', 1),
    (3, 'AUDITOR', '审核员', '审核、发布和下架资源', 1),
    (4, 'ADMIN', '管理员', '系统全部管理能力', 1);

INSERT INTO sys_permission (id, code, name, module, http_method, path, description, status) VALUES
    (1, 'resource:download', '下载资源', 'resource', 'GET', '/api/resources/{id}/download', '下载已发布资源附件', 1),
    (2, 'resource:create:own', '创建本人资源', 'resource', 'POST', '/api/author/resources', '作者创建资源', 1),
    (3, 'resource:edit:own', '编辑本人资源', 'resource', 'PUT', '/api/author/resources/{id}', '作者编辑本人可编辑资源', 1),
    (4, 'resource:submit:own', '提交本人资源', 'resource', 'POST', '/api/author/resources/{id}/submit', '作者提交审核', 1),
    (5, 'resource:withdraw:own', '撤回本人资源', 'resource', 'POST', '/api/author/resources/{id}/withdraw', '作者撤回待审核资源', 1),
    (6, 'resource:offline:own', '下架本人资源', 'resource', 'POST', '/api/author/resources/{id}/offline', '作者下架已发布资源', 1),
    (7, 'resource:delete:own', '删除本人资源', 'resource', 'DELETE', '/api/author/resources/{id}', '作者删除可删除资源', 1),
    (8, 'resource:audit:list', '查看审核队列', 'audit', 'GET', '/api/admin/resources', '查看资源审核列表', 1),
    (9, 'resource:audit', '审核资源', 'audit', 'POST', '/api/admin/resources/{id}/audit', '通过或驳回资源', 1),
    (10, 'resource:publish', '批量发布资源', 'audit', 'POST', '/api/admin/resources/batch-publish', '批量发布待审核资源', 1),
    (11, 'resource:offline:any', '管理资源下架', 'audit', 'POST', '/api/admin/resources/{id}/offline', '管理员或审核员下架资源', 1),
    (12, 'resource:delete:any', '管理删除资源', 'audit', 'DELETE', '/api/admin/resources/{id}', '管理员删除可删除资源', 1),
    (13, 'category:manage', '分类管理', 'category', NULL, '/api/admin/categories/**', '维护分类树', 1),
    (14, 'user:manage', '用户与角色管理', 'user', NULL, '/api/admin/users/**', '维护用户状态和角色', 1),
    (15, 'log:view', '查看操作日志', 'log', 'GET', '/api/admin/logs', '查看操作审计日志', 1),
    (16, 'review:moderate', '管理资源评价', 'review', 'PATCH', '/api/admin/reviews/{id}/visibility', '隐藏或恢复资源评价', 1);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
INNER JOIN sys_permission p ON p.code IN ('resource:download')
WHERE r.code = 'USER';

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
INNER JOIN sys_permission p ON p.code IN (
    'resource:download',
    'resource:create:own',
    'resource:edit:own',
    'resource:submit:own',
    'resource:withdraw:own',
    'resource:offline:own',
    'resource:delete:own'
)
WHERE r.code = 'AUTHOR';

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
INNER JOIN sys_permission p ON p.code IN (
    'resource:download',
    'resource:audit:list',
    'resource:audit',
    'resource:publish',
    'resource:offline:any'
)
WHERE r.code = 'AUDITOR';

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'ADMIN';

-- All seeded accounts use the plain-text password: password
INSERT INTO sys_user (id, username, password_hash, nickname, email, status) VALUES
    (1, 'admin', '$2a$10$Kgh4nvqquOnxS/lqZZQ6DeJCbbVRvHlohTYNDNNh1y8rd/T73LNiy', '系统管理员', 'admin@courseshare.local', 1),
    (2, 'auditor', '$2a$10$Kgh4nvqquOnxS/lqZZQ6DeJCbbVRvHlohTYNDNNh1y8rd/T73LNiy', '课程审核员', 'auditor@courseshare.local', 1),
    (3, 'author', '$2a$10$Kgh4nvqquOnxS/lqZZQ6DeJCbbVRvHlohTYNDNNh1y8rd/T73LNiy', '资源作者', 'author@courseshare.local', 1),
    (4, 'student', '$2a$10$Kgh4nvqquOnxS/lqZZQ6DeJCbbVRvHlohTYNDNNh1y8rd/T73LNiy', '学习用户', 'student@courseshare.local', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
    (1, 4),
    (2, 3),
    (3, 2),
    (4, 1);

INSERT INTO cms_category (id, parent_id, name, level, sort_order, status, description) VALUES
    (1, NULL, '编程开发', 1, 10, 1, '编程语言与工程实践'),
    (2, NULL, '数据库', 1, 20, 1, '数据库基础与性能优化'),
    (3, NULL, '前端技术', 1, 30, 1, 'Web 前端与交互开发'),
    (4, NULL, '框架技术', 1, 40, 1, '主流应用框架学习资料'),
    (5, 1, 'Java', 2, 10, 1, 'Java 语言与生态'),
    (6, 4, 'Spring', 2, 10, 1, 'Spring 与 Spring MVC'),
    (7, 3, 'Vue', 2, 10, 1, 'Vue 3 相关资源'),
    (8, 2, 'MySQL', 2, 10, 1, 'MySQL 8 相关资源');

INSERT INTO cms_article (
    id, title, summary, content, category_id, author_id, status,
    audit_by, audit_at, published_at, view_count, download_count
) VALUES
    (
        1,
        'Spring MVC 请求处理流程速览',
        '从 DispatcherServlet 到 Controller，再到统一响应与异常处理的核心链路。',
        '# Spring MVC 请求处理流程\n\n本资源梳理传统 Spring MVC 项目的请求入口、拦截器、参数绑定、Controller 与统一异常处理。\n\n## 学习建议\n\n1. 先理解 `DispatcherServlet` 的职责。\n2. 再观察拦截器与 AOP 的执行顺序。\n3. 最后通过统一响应结构验证异常路径。\n',
        6,
        3,
        2,
        2,
        CURRENT_TIMESTAMP(3),
        CURRENT_TIMESTAMP(3),
        18,
        4
    ),
    (
        2,
        'MySQL 联合索引与查询优化清单',
        '围绕筛选条件、排序字段和分页查询设计联合索引，并识别常见的 N+1 问题。',
        '# MySQL 查询优化清单\n\n## 索引设计\n\n- 将等值条件放在联合索引前部。\n- 排序字段与筛选字段组合设计。\n- 使用 `EXPLAIN` 验证执行计划。\n\n## 分页优化\n\n列表应保持一条 count 与一条联表数据查询，避免逐条加载作者和分类。\n',
        8,
        3,
        2,
        2,
        CURRENT_TIMESTAMP(3),
        CURRENT_TIMESTAMP(3),
        32,
        9
    );

