# backend-blog

十二个人博客的 Spring Boot 后端，正在从 Thymeleaf 混合项目迁移为 JSON API。

前端仓库：[front-blog](https://github.com/jam0531/front-blog)。迁移期间仍保留旧页面和其所需静态资源，待新前端验收后再退役。

## 技术与进度

- Java 8、Spring Boot 2.7.1、MyBatis-Plus 3.5.1、MySQL。
- 9 个实体统一使用 ASSIGN_ID，业务关联通过普通 ID 字段完成。
- 已实现 GET /api/home、GET /api/blogs、GET /api/blogs/{id} 及公共 DTO、JSON 错误响应。
- 新公开文章接口只返回已发布内容，所有业务 ID 以字符串输出，不暴露用户密码、邮箱或反向关联。
- 阅读量通过专用原子 SQL 更新，不改正文和更新时间。公开正文转义原始 HTML、过滤危险链接，保留 Markdown 表格和标题。

新 API、后台预览和旧详情入口统一使用安全 Markdown 渲染。旧详情仍保留标题锚点与 Semantic UI 表格样式；原始 HTML 转义为文本，危险链接被过滤，外链包含 `noopener noreferrer`。数据库中的 Markdown 原文不变。

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

本仓库不包含既有业务数据。`docs/database/2026-10-16-navigation/` 提供导航配置新表及五项初始菜单的手工 SQL，执行前需确认目标数据库、表不存在和操作授权；不会随应用启动自动执行。其他数据库脚本是历史操作记录，不应直接执行。

## 本地只读联调

使用已有 Maven 和 settings.xml，优先离线运行；本地缓存不足时需先确认是否允许下载。

```sh
mvn -o -Dmaven.test.skip=true compile
mvn -o -Dmaven.test.skip=true spring-boot:run "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=18080 --spring.profiles.active=local --spring.datasource.hikari.read-only=true --spring.sql.init.mode=never --blog.reading-count-enabled=false"
```

没有 application-local.yml 时，改用 dev profile 并设置上述环境变量。若本机 Maven 配置未自动指向原有 settings.xml，应通过 `-s` 指定自己的已有配置，不要提交包含凭据的 settings.xml。

只读模式关闭阅读计数，并要求连接只读。正常运行计数默认开启，访问详情会写入 views；执行任何实际写库联调前应先确认数据环境和授权。

## API

- `/api/home`：公告、推荐文章、分类排行、统计和年份汇总；不再查询或返回未被当前首页使用的 `background`、`latestBlogs`。
- `/api/background`：欢迎页独立获取随机背景，返回 `id`、`picName`、`piclink`；无图片时为 null。
- `/api/blogs?page=1&size=10`：摘要分页，size 最大 100。
- `/api/blogs/{id}`：公开详情及安全 HTML，未发布/不存在返回 404。

前后端可以独立构建、部署，但接口字段仍需兼容。本次首页精简与当前 `front-blog` 兼容：首页文章由 `/api/blogs` 分页提供，欢迎页由 `/api/background` 提供；当前前端也能读取尚未精简的旧后端。若服务器仍使用依赖 `background`、`latestBlogs` 的历史前端，应先升级前端并确认旧缓存已更新，再部署精简后的后端。回滚到这类历史前端时，需先恢复保留旧字段的后端。旧 Thymeleaf 页面不通过该聚合接口取数，其查询不受影响。

参见 [公开接口说明](docs/api/phase1-read-api.md)、[认证与文章管理](docs/api/phase3-admin-api.md)、[相册友链和资源管理](docs/api/phase4-resources-api.md)、[评论留言](docs/api/phase5-discussions-api.md)、[当前进度](docs/separation-progress.md) 与 [分阶段改造计划](docs/superpowers/plans/2026-10-15-vue3-separation-plan.md)。

“更多”菜单由独立的 `site_navigation` 表管理，支持名称、关联页面、顺序和显隐。隐藏或删除配置不关闭原页面，不删除业务内容。接口与发布顺序见 [导航管理](docs/api/navigation-api.md)。

登录失败保护使用单实例内存计数，不写数据库、不依赖 Redis。默认从首次尝试起的 15 分钟窗口内，同一账号（忽略大小写、首尾空格）和 IP 最多失败 5 次，同一 IP 最多失败 30 次；达到上限后，窗口剩余时间内返回 HTTP 429、中文提示和 `Retry-After` 秒数。并发验证占用名额；验证成功清除该账号/IP失败记录，服务故障不计为密码错误。已有登录会话不受影响。计数最多保留 10000 个键，容量耗尽时拒绝新键并提示稍后重试；重启清空计数，多实例不共享计数。

可通过 `blog.login-protection.window-seconds`（默认 900）、`account-ip-failures`（默认 5）、`ip-failures`（默认 30，均在同一配置前缀下）调整限制，值必须大于 0。客户端 IP 使用 `request.getRemoteAddr()`；生产环境继续要求后端仅允许可信代理连接，代理覆盖转发头，不能直接暴露启用转发头解析的后端端口。

API 参数校验按服务端字段白名单输出中文字段名及实际约束，每次最多展示三个不同提示；同时覆盖 JSON 输入、查询参数和路径参数的校验或类型错误。不回显密码、提交值、Java类型、SQL或内部异常文本，响应仍使用原来的 `code/message/data` 格式。

生产可使用 `prod` profile，必须配置 BLOG_DB_URL、BLOG_DB_USERNAME、BLOG_DB_PASSWORD，默认只监听本机 18080 且使用 Secure Session Cookie，需要可信 HTTPS 反向代理。前端仓库的 `docs/deployment.md` 和 `deploy/nginx.conf.example` 包含独立发布、缓存与回滚说明。没有实际应用到服务器。

## 验证与资源说明

Java 离线编译已通过。公开浏览、资源管理、认证、文章管理和评论留言已完成相应 HTTP/页面检查。用户分别授权了三组临时数据联调：文章/分类/标签，公告/图片/友链，2 篇文章及各 6 条评论/留言。已覆盖草稿发布、字段关联、权限、限频、关闭评论后的后台回复及级联删除；三组临时记录均已清理，每组相关表完整数据指纹与操作前一致，恢复本地只读参数。实际阅读量写入、非管理员真实账号、多进程并发及生产部署未验收。没有新增或修改测试代码。

日志、构建产物、IDE 文件、数据库本地配置及约 600 MB 的历史音乐资源不纳入 Git；音乐保留在原工程和本机副本，旧播放器在全新克隆后需另行配置资源。图片、样式、第三方前端库等旧页面依赖暂时保留。
