# backend-blog

十二个人博客的 Spring Boot 后端，正在从 Thymeleaf 混合项目迁移为 JSON API。

前端仓库：[front-blog](https://github.com/jam0531/front-blog)。迁移期间仍保留旧页面和其所需静态资源，待新前端验收后再退役。

## 技术与进度

- Java 8、Spring Boot 2.7.1、MyBatis-Plus 3.5.1、MySQL。
- 9 个实体统一使用 ASSIGN_ID，业务关联通过普通 ID 字段完成。
- 已实现 GET /api/home、GET /api/blogs、GET /api/blogs/{id} 及公共 DTO、JSON 错误响应。
- 新公开文章接口只返回已发布内容，所有业务 ID 以字符串输出，不暴露用户密码、邮箱或反向关联。
- 阅读量通过专用原子 SQL 更新，不改正文和更新时间。公开正文转义原始 HTML、过滤危险链接，保留 Markdown 表格和标题。

- 分类、标签、标题/摘要搜索、年份归档和独立随机背景接口已完成。
- Session 登录、CSRF、文章/分类/标签管理 API 已完成；公开及后台 DTO 的 ID 均为字符串。事务维护文章和标签关联，不使用数据库外键。

公告、相册、友链、评论留言及后台管理 API 已实现并完成分组联调。生产配置与部署说明已准备，真实上线和旧入口退役尚未执行。

## 数据库配置

仓库中的 dev/pro 配置通过环境变量读取连接参数，不包含真实账号密码：

| 变量 | 说明 |
|---|---|
| BLOG_DB_URL | 默认连接本机 blog 数据库；远程环境应显式配置 |
| BLOG_DB_USERNAME | 必填，数据库账号 |
| BLOG_DB_PASSWORD | 必填，数据库密码 |

也可以自行建立 `src/main/resources/application-local.yml` 并用 local profile 启动。该文件已被 Git 忽略；本机导入时保留了原有连接配置，但不会推送到 GitHub。

本仓库不包含业务数据或数据库初始化脚本。docs/database 中是此前外键移除操作的记录及恢复脚本，不是启动脚本，不应直接执行。

## 本地只读联调

使用已有 Maven 和 settings.xml，优先离线运行；本地缓存不足时需先确认是否允许下载。

```sh
mvn -o -Dmaven.test.skip=true compile
mvn -o -Dmaven.test.skip=true spring-boot:run "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=18080 --spring.profiles.active=local --spring.datasource.hikari.read-only=true --spring.sql.init.mode=never --blog.reading-count-enabled=false"
```

没有 application-local.yml 时，改用 dev profile 并设置上述环境变量。若本机 Maven 配置未自动指向原有 settings.xml，应通过 `-s` 指定自己的已有配置，不要提交包含凭据的 settings.xml。

只读模式关闭阅读计数，并要求连接只读。正常运行计数默认开启，访问详情会写入 views；执行任何实际写库联调前应先确认数据环境和授权。

## API

- `/api/home`：背景、公告、最新/推荐文章、分类排行、统计和年份汇总。
- `/api/blogs?page=1&size=10`：摘要分页，size 最大 100。
- `/api/blogs/{id}`：公开详情及安全 HTML，未发布/不存在返回 404。

参见 [公开接口说明](docs/api/phase1-read-api.md)、[认证与文章管理](docs/api/phase3-admin-api.md)、[相册友链和资源管理](docs/api/phase4-resources-api.md)、[评论留言](docs/api/phase5-discussions-api.md)、[当前进度](docs/separation-progress.md) 与 [分阶段改造计划](docs/superpowers/plans/2026-10-15-vue3-separation-plan.md)。

生产可使用 `prod` profile，必须配置 BLOG_DB_URL、BLOG_DB_USERNAME、BLOG_DB_PASSWORD，默认只监听本机 18080 且使用 Secure Session Cookie，需要可信 HTTPS 反向代理。前端仓库的 `docs/deployment.md` 和 `deploy/nginx.conf.example` 包含独立发布、缓存与回滚说明。没有实际应用到服务器。

## 验证与资源说明

Java 离线编译已通过。公开浏览、资源管理、认证、文章管理和评论留言已完成相应 HTTP/页面检查。用户分别授权了三组临时数据联调：文章/分类/标签，公告/图片/友链，2 篇文章及各 6 条评论/留言。已覆盖草稿发布、字段关联、权限、限频、关闭评论后的后台回复及级联删除；三组临时记录均已清理，每组相关表完整数据指纹与操作前一致，恢复本地只读参数。实际阅读量写入、非管理员真实账号、多进程并发及生产部署未验收。没有新增或修改测试代码。

日志、构建产物、IDE 文件、数据库本地配置及约 600 MB 的历史音乐资源不纳入 Git；音乐保留在原工程和本机副本，旧播放器在全新克隆后需另行配置资源。图片、样式、第三方前端库等旧页面依赖暂时保留。
