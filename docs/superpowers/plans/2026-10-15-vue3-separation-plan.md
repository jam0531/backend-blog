# Vue 3 + Vite 前后端分离改造计划

> 后续进度：代码现已拆成 front-blog / backend-blog 两个仓库；本文保留最初方案，原文中“未开始”及部分映射待办属于历史状态。当前实现与验证范围以仓库 README、docs/api/phase1-read-api.md 为准。后续在各自仓库继续推进，不重复执行已完成的字段映射和 ID 策略修改。

> 状态：待审阅的实施计划，未开始改造。后续按任务逐项执行；本次仅创建本文档。实施时使用 executing-plans 流程，用户另有明确要求时以用户要求为准。不自动启动实施或委派子代理。

**Goal：** 将现有个人博客逐步改为 Vue 3 + Vite 前端与 Spring Boot JSON API 后端，先打通文章浏览，再补齐管理和互动功能。

**Architecture：** 当前 Maven 工程原地保留，在根目录新增独立 frontend 工程。迁移期间保留旧 Controller 和 Thymeleaf 模板，新 API 使用 /api 前缀；前端验收后再切换正式入口。生产环境优先使用同域名 Nginx，静态页面和 Java 服务分别发布。

**Tech Stack：** Vue 3、Vite、Vue Router；保留 Java 8、Spring Boot 2.7.1、MyBatis-Plus 3.5.1、MySQL 和现有 Markdown 转换实现。具体前端版本在实施前核对本机环境及兼容性，本计划不授权下载依赖。

**Spec：** 本次聊天中用户已选择 Vue 3 + Vite，并要求列出复用代码、接口变更和实施顺序，暂不改代码。本文兼作范围说明和实施计划；接口名称、DTO、目录及验收标准均为建议新增内容，不代表源码已经存在。

## 1. 全局约束与范围

- 本次只新增计划文档，不修改 Java、HTML、配置、数据库或依赖文件。
- 后续仍遵循最小变更原则，不同时升级 Spring Boot、Java、MyBatis-Plus，不整包重命名，不进行无关重构。
- 未经用户明确要求，不创建或修改任何测试类、测试脚本、Mock 或测试文件。本文验收均为人工核对、已有检查或浏览器/API 联调。
- 数据库默认只读。任何数据写入、建表、关系表改造、密码迁移、清理或数据修复，都必须另行说明影响和回滚方式并取得明确授权；计划获认可不等于获得数据库执行授权。
- 不自动安装 Node.js、npm 依赖、Maven 依赖、插件或其他工具。先核对本地环境与缓存，缺失时说明并等待授权；不创建临时构建配置绕过问题。
- 不把 API 实体直接序列化给前端；密码、凭据、内部异常和不应公开的邮箱不进入公开响应。
- 首期采用 SPA；SSR、静态生成、复杂状态框架、独立网关、文件上传、注册找回密码、多角色管理、Redis 业务缓存不在本次默认范围。
- 首期复现现有页面的主要信息和交互，不同时进行视觉改版。图片仍以已有地址引用为主，不推导出上传需求。
- 对缺失业务先约定规则再实现，不把页面意图当成已经确认的业务行为。

## 2. 当前代码基线

本结论来自源码静态检查，没有启动服务或访问数据库。

| 当前入口 | 源码行为 | 迁移安排 |
|---|---|---|
| GET / | 随机查询背景图片，返回 index | 新增首页信息 API，Vue 欢迎页使用 |
| GET /home | 查询文章分页、最新文章、推荐、公告、分类、归档及统计 | 拆为首页信息 API 与文章分页 API |
| POST /publishBlogs | 查询已发布文章，返回 home 的 HTML 片段 | 改由新的 GET /api/blogs 返回 JSON；此接口不是发布文章 |
| GET /blog/{id} | 查询文章，Markdown 转 HTML，阅读量加一，返回 blog | 新增公开文章详情 API；旧 URL 交给 Vue Router 保留 |

以上入口集中在 `src/main/java/com/zhj/blog/Controller/IndexController.java`。当前未发现后台、搜索、分类详情、评论、留言、友链或相册的请求 Controller。它们属于“页面/基础代码已存在，但需补齐后端功能”。

已确认的衔接问题：

- `Service/TypeServiceImp.java` 的 TypeTopList 查询后返回 null；`Service/TagServiceImp.java` 的 TopTagList 也返回 null。
- `Pojo/Blog.java` 错误导入 java.lang.reflect.Type；作者、分类、标签等为非表字段，当前查询没有补充关联数据。
- `templates/home.html` 使用 page.records，但分页仍读取 page.totalPages、提交从 0 开始的 page 参数，未与后端分页契约统一。
- `Service/BlogServiceImp.java` 的详情和归档没有发布状态过滤；按年份的归档重载对 LocalDateTime 使用 SimpleDateFormat，需要调整。
- 详情使用整实体更新阅读量；返回的文章副本在加一前已创建。新 API 需明确返回计数，并避免阅读量更新顺带改正文和更新时间。
- `Pojo/Comment.java`、`Pojo/Message.java` 混有对象关系字段，需要与真实外键列核对后再用于查询和写入，不能假定 JPA 注解能让 MyBatis-Plus 自动加载关系。
- `Service/UserService.java` 是空接口，当前没有完整登录校验逻辑；不能把 Session 拦截器当成已完成登录功能。
- 当前未发现初始化 SQL。TypeMapper 还依赖 type_count，其类型、定义和统计口径需要只读核对。

## 3. 代码复用及修改清单

表中 Java 路径均相对于 `src/main/java/com/zhj/blog/`。

| 代码/资源 | 复用程度 | 具体安排 |
|---|---|---|
| BlogApplication.java、pom.xml | 基本保留 | 延用启动入口和既有后端版本；新增必要依赖须单独评估和授权 |
| Mapper/BlogMapper.java 等九个 Mapper | 基础 CRUD 可复用 | 按核实后的表结构补充关系查询、统计和受控写入；不新增泛化 Repository 层 |
| Service/BlogService.java、BlogServiceImp.java | 查询逻辑部分复用 | 保留发布列表、最新、推荐及年份归档思路；补 DTO 组装、公开状态限制、管理写入、搜索和阅读量专用更新 |
| Service/TypeService*、TagService* | 基础类复用 | 修正空返回，核对分类统计和标签关联；不强制新增页面未使用的热门标签功能 |
| Service/NoticeServiceImp.java | 最新公告查询复用 | 空公告返回 null；补后台公告管理 |
| Service/PictureServiceImp.java、Mapper/PictureMapper.java | 随机查询复用 | 无图片时前端使用默认背景；后续补分页、日期筛选和后台管理 |
| Service/UserService*、Mapper/UserMapper.java | 数据访问基础复用 | 新增凭据校验、管理员资格检查、会话管理；密码格式及 user.type 含义先核对 |
| Service/CommentService*、MessageService*、FriendlinkService* | 基础 CRUD 骨架复用 | 新增业务校验、列表/回复组装及所需管理方法 |
| Pojo/*.java | 标量字段及已确认表映射复用 | 只修复当前接口所需映射；不得臆造外键、关系表或级联删除规则 |
| Utils/MarkdownUtils.java | 转换逻辑复用 | 统一展示结果和安全过滤策略；数据库继续保留 Markdown 原文 |
| Configuration/MyMetaObjectHandler.java | 按现有规则复用 | 核对新增记录时间字段；阅读量更新不得误触文章更新时间。其他时间语义调整单独列出 |
| interceptor/MybatisPlusConfig.java | 保留有效配置 | 使用现有分页配置；不引入第二个同名 Bean |
| interceptor/LoginInterceptor.java、WebConfig.java | 旧页面逻辑暂保留 | 新增 API 专用鉴权范围与 JSON 401/403，避免重定向成登录 HTML |
| handler/ExceptionHandler.java | 旧页面处理暂保留 | 新增仅作用于 API Controller 的异常处理；限制旧处理器范围/确定优先级，确保 API 不返回错误 HTML |
| Configuration/RedisConfig.java、Utils/RedisUtil.java | 暂保留 | 分离首期不新增缓存用法，也不假定 Session 已存入 Redis |
| templates/*.html、templates/admin/*.html | 复用布局和交互需求 | 改写为 Vue 页面；th:*、Model 和 session.user 表达式改为 API 数据 |
| templates/fragments.html、admin/_fragments.html | 复用公共区域设计 | 改成站点布局、后台布局、导航、页脚等组件 |
| static/css、images、字体等 | 按页面选择复用 | 避免所有全局样式一起加载；统一路径，保留必要响应式效果 |
| static/lib/editormd、Aplayer 等 | 可封装复用 | 不照搬页面内脚本。Editor.md 可暂封装在编辑器组件中并局部保留其依赖；播放器也独立封装，按本地库能力核对初始化和清理 |
| static/js 中动画、事件脚本 | 逐项迁移 | 仅接入实际需要的效果，处理切页后的事件、定时器和播放器重复初始化 |

## 4. 建议新增文件和职责

### 4.1 前端

新增 `frontend/`，与 Maven 工程独立构建。先用 JavaScript，避免同时引入额外语言迁移；请求优先使用封装后的原生 fetch，首期不强制增加 Axios 或 Pinia。

| 建议文件/目录 | 职责 |
|---|---|
| frontend/package.json、vite.config.js、index.html | 工程入口、构建命令和开发代理 |
| frontend/src/main.js、App.vue | Vue 应用入口 |
| frontend/src/router/index.js | 前台/后台路由、登录跳转、404 |
| frontend/src/api/http.js | /api 基址、JSON 解析、错误处理、401 和 CSRF 请求头 |
| frontend/src/api/blog.js、home.js、auth.js | 文章、首页、登录 API |
| frontend/src/api/admin.js、interaction.js、resources.js | 后台管理、评论留言、分类/图片等 API；随阶段新增 |
| frontend/src/state/auth.js | 小范围共享当前会话状态，服务器始终是鉴权依据 |
| frontend/src/layouts/SiteLayout.vue、AdminLayout.vue | 访客与后台布局 |
| frontend/src/components/BlogCard.vue、Pagination.vue、MarkdownContent.vue | 文章摘要、统一分页、受控 HTML 展示 |
| frontend/src/components/MarkdownEditor.vue、MusicPlayer.vue | 隔离旧插件和生命周期，后续阶段接入 |
| frontend/src/views/CoverView.vue、HomeView.vue、BlogDetailView.vue | 首批访客页面 |
| frontend/src/views/TypesView.vue、TypeBlogsView.vue、SearchView.vue、ArchivesView.vue | 分类、搜索和归档 |
| frontend/src/views/MessageView.vue、FriendLinksView.vue、AlbumView.vue、PictureView.vue、AboutView.vue、AboutWebView.vue | 扩展页面 |
| frontend/src/views/admin/LoginView.vue、BlogListView.vue、BlogEditView.vue | 登录和文章管理 |
| frontend/src/views/admin/TypeListView.vue、TagListView.vue、NoticeListView.vue、PictureListView.vue、FriendLinkListView.vue、CommentListView.vue、MessageListView.vue | 其余管理页面；简单编辑可使用弹窗，不为复刻旧文件名强行拆页 |
| frontend/src/assets/、frontend/public/ | 构建资源和必须保持固定地址的资源 |

前台尽量保留 `/`、`/home`、`/blog/:id`、`/types`、`/type/:id`、`/search`、`/archives`、`/archive/:year`、`/message`、`/friend-links`、`/album`、`/albumArchive/:date`。核对 picture 模板的实际入口后确定图片详情 URL；关于页面统一选定入口并保留现有别名。未完成的页面入口暂不对外启用。

### 4.2 后端

新 API 放在 `Controller/api/`，与旧 `IndexController` 并存：

- `HomeApiController.java`、`BlogApiController.java`：首页和公开文章。
- `CatalogApiController.java`：分类、标签和归档。
- `AuthApiController.java`：CSRF、登录、当前用户、退出。
- `AdminBlogApiController.java`：文章管理和预览。
- `CommentApiController.java`、`MessageApiController.java`：公开评论和留言。
- `ResourceApiController.java`：友链与相册查询。
- `AdminTypeApiController.java`、`AdminTagApiController.java`、`AdminNoticeApiController.java`、`AdminPictureApiController.java`、`AdminFriendLinkApiController.java`：相应资源管理。
- `AdminCommentApiController.java`、`AdminMessageApiController.java`：互动管理。

配套按阶段新增 `Dto/ApiResponse.java`、`Dto/PageResult.java` 及各模块 DTO，`handler/ApiExceptionHandler.java`、`interceptor/ApiLoginInterceptor.java`、`interceptor/ApiCsrfInterceptor.java`。沿用现有 Service/Mapper，不新增通用 CRUD Controller 或复杂抽象框架。

DTO 分组建议：`Dto/blog/`、`Dto/home/`、`Dto/auth/`、`Dto/catalog/`、`Dto/interaction/`、`Dto/resource/`。只在所属阶段创建实际需要的请求/响应类。

## 5. 接口公共约定

以下为建议契约，阶段 0 审阅后固定，不能在前后端实现时各自改变。

| 项目 | 约定 |
|---|---|
| 前缀 | 所有新后端接口使用 /api；管理写操作使用 /api/admin；鉴权使用 /api/auth |
| 成功响应 | `{code: 0, message: "success", data: ...}`，由 HTTP 2xx 表示成功；删除成功 data 为 null |
| 错误响应 | 使用 400/401/403/404/409/500 等真实 HTTP 状态，并返回 code、message、data；不向前端返回堆栈、SQL、密码或登录 HTML |
| 分页 | page 从 1 开始，size 默认 10、最大 100；返回 records、total、page、size。前端按 total/size 计算页数 |
| 排序 | 公开列表保留 updateTime 倒序，最新/推荐保留 createTime 倒序，增加 id 倒序保证同时间记录顺序稳定 |
| ID | JSON 中的业务 ID 统一十进制字符串，避免 Java Long 到 JavaScript Number 的精度损失 |
| 时间 | LocalDateTime 按 `yyyy-MM-dd'T'HH:mm:ss` 输出；不擅自加 Z 或转换历史数据时区，站点时区在阶段 0 核对 |
| 公开数据 | 建议公开文章、归档、搜索、统计都只包含 publish=true；这是对当前详情/归档遗漏的行为修正，需纳入计划审阅 |
| 参数校验 | 固定允许的筛选字段；page/size、ID、状态和内容长度在后端校验，不接受客户端任意 SQL 字段 |
| 登录 | 首期 Session Cookie；登录成功轮换会话 ID，退出销毁会话；Cookie 的 Secure/HttpOnly 等按 HTTPS 环境配置 |
| 管理权限 | 检查会话身份和实际管理员资格；user.type 含义未核对前不得凭猜测授权 |
| CSRF | 会话绑定 token，经 GET /api/auth/csrf 获取；登录、退出及其他非安全方法校验 X-CSRF-Token，token 不写入日志或 URL |
| 内容 | 数据库存 Markdown；公开详情输出经过确定安全策略处理的 contentHtml；后台编辑读取 contentMarkdown |
| 默认值 | 缺少分类/作者/图片时返回明确 null 或空集合，前端有占位；不编造作者、分类或图片记录 |

关键数据结构：

- `BlogSummaryDto`：id、title、description、blogPic、flag、views、createTime、updateTime、author{id, aliasname, avatar}、type{id, typeName}、tags[{id, tagName}]。不返回正文或 User 实体。
- `BlogDetailDto`：在摘要字段上增加 contentHtml、shareStatement、commentAble；评论独立分页。当前读取详情计数方式保留为每次成功的公开详情请求加一，改为数据库单条原子更新，仅改 views；返回本次更新后的计数，不把它当唯一访客数。后续修改计数口径需另确认。
- `BlogEditDto`：id、title、description、blogPic、flag、contentMarkdown、typeId、tagIds、publish、recommend、shareStatement、commentAble。不把 views、userId、创建时间等服务端字段作为普通表单可写字段。
- `HomeDto`：background、notice、latestBlogs（5 篇）、recommendBlogs（6 篇）、topTypes（5 项）、archiveYears、stats{publishedBlogCount,typeCount,tagCount}。分类/标签总数沿用全部分类/标签的页面口径；排行统计定义先核实，缺失时不臆造算法。
- `CommentDto` / `MessageDto`：id、parentId、昵称、头像、正文、时间和管理员标识；留言另含 messageType。公开响应不含邮箱；回复结构及分页粒度在阶段 0 固定。
- 新增留言/评论请求使用 nickname、email、content、parentId（无父级为 null），关联 ID 用标量；管理员身份、头像默认值及时间由后端决定，不信任请求中的管理员标识。

## 6. 接口清单

所有 /api 接口均为新增；“复用”指复用现有内部方法，不指已有同名 API。旧页面接口在过渡期不直接改成 JSON。

### 6.1 第一批：访客文章浏览

| 方法与路径 | 请求/响应重点 | 后端来源与改造 |
|---|---|---|
| GET /api/home | 返回 HomeDto；无图片、公告也能响应 | 复用随机图片、最新公告、最新/推荐文章；修复分类查询，新增轻量统计及归档年份汇总 |
| GET /api/blogs | page、size；返回 PageResult<BlogSummaryDto> | 复用 PublishedBlogList 的筛选和排序，补作者/分类/标签组装，按 ID 集合批量查询可合并的关联数据 |
| GET /api/blogs/{id} | 返回 BlogDetailDto | 复用 Markdown 转换；校验发布状态，修正关联映射及阅读量更新；未发布/不存在统一 404 |

这批完成即可验收“欢迎页 → 首页 → 分页 → 文章详情”，不依赖后台或评论功能上线。

### 6.2 第二批：分类、搜索和归档

| 方法与路径 | 请求/响应重点 | 新增/复用范围 |
|---|---|---|
| GET /api/types | 分类列表及已发布文章数量 | 复用 TypeMapper，核实关系后新增计数 |
| GET /api/tags | 标签列表 | 复用 TagMapper；首期不额外新增标签排行 |
| GET /api/blogs | 增加 typeId、tagId、keyword、year 可选参数 | 在第一批接口上扩展；字段组合、搜索范围先确认，公开结果始终排除草稿 |
| GET /api/archives | 年份及已发布文章数量 | 复用归档思路，按年份汇总；年内文章复用 /api/blogs?year=... 分页 |

搜索页面使用 /api/blogs?keyword=...，不另建重复的搜索分页协议。搜索建议先限标题和摘要；是否含正文由用户确认。

### 6.3 第三批：认证及文章管理

| 方法与路径 | 用途/响应 | 实施要求 |
|---|---|---|
| GET /api/auth/csrf | 获取当前会话 CSRF token | 未登录也可获取；与 Session 绑定 |
| POST /api/auth/login | username、password → 当前用户摘要 | 核实密码存储方式、管理员资格和失败提示；禁止返回密码或完整实体 |
| GET /api/auth/me | 当前用户摘要 | 未登录返回 401 |
| POST /api/auth/logout | 退出当前会话 | 校验 CSRF，销毁会话 |
| GET /api/admin/blogs | page、size、keyword、typeId、publish、recommend | 管理员可查看草稿及已发布文章 |
| GET /api/admin/blogs/{id} | 返回 BlogEditDto | 返回 Markdown 原文，不增加阅读量 |
| POST /api/admin/blogs | 新增草稿或发布文章 | 请求为编辑字段，不包含服务端生成字段；返回保存后的 id 和状态 |
| PUT /api/admin/blogs/{id} | 更新文章及分类、标签、发布状态 | 不另建一套发布接口；完整编辑语义，关系保存与正文保存使用事务 |
| DELETE /api/admin/blogs/{id} | 删除文章 | 评论和关联记录如何处理必须先确认；规则未确认时不实现删除 |
| POST /api/admin/blogs/preview | contentMarkdown → contentHtml | 不保存文章，不增加阅读量，复用详情安全转换规则 |

分类、标签下拉数据使用下一节的管理列表，不直接依赖公开统计接口。初期不新增批量发布、定时发布、账号注册或密码重置。

### 6.4 第四批：基础内容管理

以下每种资源分别新增五个接口：`GET /api/admin/{resource}`（分页列表）、`GET /api/admin/{resource}/{id}`（编辑详情）、`POST /api/admin/{resource}`（新增）、`PUT /api/admin/{resource}/{id}`（更新）、`DELETE /api/admin/{resource}/{id}`（删除）。

| resource 固定值 | 可编辑字段 | 复用类/注意点 |
|---|---|---|
| types | typeName | TypeService/TypeMapper；有关联文章时的删除规则待确认 |
| tags | tagName | TagService/TagMapper；关系删除规则待确认 |
| notices | content | NoticeService/NoticeMapper；首页继续按创建时间取最新一条 |
| pictures | picName、piclink、picDescription | PictureService/PictureMapper；地址引用，不包含上传能力 |
| friend-links | friendlinkName、friendlinkAddress、friendlinkAvater、friendlinkDescription | FriendlinkService/FriendLinkMapper；保留实体既有字段拼写，暂不做无关重命名 |

类型/标签供编辑器选择时，客户端遍历分页取得完整选项，不允许把默认第一页当作全部选项。需要专用轻量选项接口时再依据实际数据量决定，不预先增加重复接口。

types/tags 的基础管理需要在文章发布联调前完成，其余三类可后续执行。“第四批”是接口分类，不表示文章管理可以跳过分类/标签依赖。

### 6.5 第五批：评论和留言

| 方法与路径 | 用途 | 约束 |
|---|---|---|
| GET /api/blogs/{id}/comments | 公开评论及回复 | 核对公开可见性和分页规则；文章必须已发布 |
| POST /api/blogs/{id}/comments | 新增评论或回复 | 校验 commentAble、父评论归属；服务端判定管理员标识 |
| GET /api/messages | 分页获取留言，参数 messageType | 模板中 0 为普通留言、1 为友链页面留言 |
| POST /api/messages | 新增留言或回复 | 校验父留言与 messageType 一致；不自动创建友链记录 |
| GET /api/admin/comments | 评论管理列表，可按 blogId 筛选 | 管理鉴权；邮箱如有管理用途仅在此返回 |
| DELETE /api/admin/comments/{id} | 删除评论 | 子回复处理规则确认后实现 |
| POST /api/admin/comments/{id}/replies | 管理员回复 | 父级、文章关联由服务端查询；关闭评论时能否管理员回复需确认 |
| GET /api/admin/messages | 留言管理列表，可按 messageType 筛选 | 管理鉴权 |
| DELETE /api/admin/messages/{id} | 删除留言 | 子回复处理规则确认后实现 |
| POST /api/admin/messages/{id}/replies | 管理员回复 | messageType 与父级保持一致 |

首期不自行增加审核状态字段、验证码平台、邮件通知或审核后台。是否需要先审后显、防滥用策略，在开放公开写接口前确认；不能把“当前无实现”当成默认允许匿名无限写入。

### 6.6 第六批：友链、相册和内容页

| 方法与路径 | 用途 | 实现范围 |
|---|---|---|
| GET /api/friend-links | 公开友链列表 | 复用 FriendLinkMapper，确定排序 |
| GET /api/pictures | page、size、date 可选 | 图片列表与按日期筛选；date 格式 yyyy-MM-dd，分组依据 createTime，站点时区核实后固定 |
| GET /api/pictures/{id} | 图片详情 | 核对现有 picture 模板所需字段后接入 |
| GET /api/picture-archives | 日期及图片数量 | 新增分组查询，支持现有相册按日期导航 |

关于作者、关于网站可先迁移为 Vue 内容页；音乐歌单沿用现有前端配置，不默认新增后台配置接口。若用户需要在线维护这些内容，再独立定义需求。

## 7. 实施任务与验收顺序

### 阶段 0：核对数据结构与固定规则

**读取范围：** 本文列出的实体、Mapper、Service、对应模板；必要时由用户提供数据库连接信息或结构导出。此阶段不修改业务代码。

- [ ] 只读核对九个实体表，以及文章—分类、文章—标签、文章—作者、评论/留言父级关系的真实列和关联表。
- [ ] 核对 type_count 的定义、统计口径，登录密码存储算法和 user.type 含义；不输出密码值。
- [ ] 确认公开草稿不可见、阅读计数与更新时间分离这两项行为修正。
- [ ] 固定搜索范围、回复层级及分页方式、各类删除与子记录处理规则；缺少回答时只暂停相应写接口，不阻塞已明确的浏览模块。
- [ ] 确认评论/留言展示与防滥用规则、文章编辑必填项和字段长度；以数据库长度和现有表单为依据，不编造限制。
- [ ] 核对站点时区和相册日期格式；确认现有图片链接及资源访问方式。
- [ ] 核对本地 Node/npm、Java/Maven、已有配置及缓存，列出需要授权安装的依赖，不自动下载。

**交付/验收：** 一份实际字段与关系映射、确定的接口契约及尚未解决的模块级阻塞项；不以猜测生成 DDL。不存在关系结构时先报告并等待数据库方案授权。

### 阶段 1：文章浏览 API 与 Vue 最小主线

**文件：** 新增 HomeApiController、BlogApiController、公共与文章/首页 DTO、API 异常处理；修改 Blog/Type 所需映射及相关 Service/Mapper；新增 frontend 工程入口、请求封装、路由、三张浏览页和基础组件。

- [ ] 在用户允许的本地环境下创建前端工程，配置 /api 开发代理；具体 Node/Vite 版本与锁文件一并确定。
- [ ] 实现第 6.1 节三个接口；API 异常处理与旧 HTML 异常处理分开，不改变旧 Controller 的返回类型。
- [ ] 修复关联数据、分类空返回、分页协议和公开文章过滤；仅对所需查询做批量关联组装。
- [ ] 处理 Markdown 输出安全及阅读量专用更新；说明数据库写入影响，待获授权后才进行会实际增加阅读量的联调。
- [ ] Vue 打通欢迎页、首页、分页、文章详情，以及加载中、空列表、失败、404 状态。

**验收：** 浏览三页主线；末页/越界页可解释，缺失分类或图片不崩溃；草稿不能通过公开 ID 读取；404/500 为 JSON；阅读量更新不会修改正文或影响文章更新时间。涉及写库的验证只在明确授权的隔离环境进行。

### 阶段 2：分类、搜索和归档

**文件：** 新增 CatalogApiController 及其 DTO；扩展 BlogService/Mapper；新增 TypesView、TypeBlogsView、SearchView、ArchivesView。

- [ ] 按第 6.2 节增加筛选及汇总，使用同一分页响应。
- [ ] 修复年份处理，确认分类/标签计数与公开文章过滤一致。
- [ ] 将筛选和页码保存在路由查询参数中，支持刷新、前进后退及可复制链接。

**验收：** 分类筛选、关键词、年份筛选与分页组合正确；空结果有提示；归档和搜索不泄露草稿；网页刷新后条件仍可恢复。

### 阶段 3：登录、分类标签管理与文章发布

**文件：** 新增 AuthApiController、AdminBlogApiController、AdminTypeApiController、AdminTagApiController、相关 DTO、API 登录/CSRF 拦截器；修改 UserService、BlogService 及关联 Mapper；新增会话状态、后台布局、登录和文章/分类/标签管理页面。

- [ ] 实现登录、退出、当前用户、CSRF 及 /api/admin/** 全范围鉴权；不修改旧 Session 页面拦截器的响应语义。
- [ ] 先实现分类/标签管理与选择数据，再接文章编辑。
- [ ] 实现后台列表、读取 Markdown、预览、新增/更新/发布；编辑器首期可封装现有 Editor.md，避免边分离边更换编辑体验。
- [ ] 将正文与关联关系写入放入同一事务；校验合法分类、标签和字段，作者取会话身份或保留既有作者规则，不接收任意 userId。
- [ ] 删除仅在关联规则确认后实现；写库联调前取得独立授权。

**验收：** 未登录、非管理员、会话过期、非法 CSRF 均被后端拒绝；密码不进入响应；保存草稿与发布状态一致；编辑保留原作者等服务端信息；分类/标签保存一致；后台预览不计阅读量。人工验证需要写入数据时先授权。

### 阶段 4：公告、图片、友链及静态内容页

**文件：** 新增 ResourceApiController、三类管理 Controller/DTO，扩展对应 Service/Mapper；新增访客与后台页面、插件封装及所需资源。

- [ ] 实现剩余基础管理接口以及第 6.6 节公开接口。
- [ ] 迁移相册、友链、关于页面；图片继续填写地址，不新增上传存储系统。
- [ ] 按需求接入播放器和必要动画，清理重复事件、定时器及切页副作用。

**验收：** 公告最新项选择正确；友链和图片地址可用；相册日期切换/分页稳定；连续切页不重复播放或累积监听器；公开页面不暴露管理字段。

### 阶段 5：评论、留言及后台管理

**文件：** 新增四个互动 Controller、相关 DTO 和 Vue 页面/组件；按核实的数据库关系修改 Comment、Message 及 Service/Mapper。

- [ ] 规则确认后实现第 6.5 节接口；先列表，再提交/回复，最后后台删除。
- [ ] 校验父级归属、commentAble、messageType 和权限；管理员身份始终从服务端会话获取。
- [ ] 普通评论/留言按纯文本处理，邮箱不公开；接入确认后的内容展示及防滥用规则。

**验收：** 回复不能挂到其他文章或其他留言类型；普通用户不能伪造管理员；关闭评论的行为符合已确认规则；删除与子回复行为一致；分页不会重复或遗漏根评论。所有写操作验证需独立授权。

### 阶段 6：部署切换与旧页面退役

**文件：** 前端构建配置、部署说明；Nginx 配置位置由实际服务器确定，不能现在假设服务器路径。最后才清理无引用旧模板/Controller/依赖。

- [ ] 先在预览入口验收新前端，旧站继续由旧入口提供；不让 Nginx 把同一页面路径随机分配给两套前端。
- [ ] 生产环境由 Nginx 提供 dist，/api/ 保留前缀转发 Spring Boot，其他前端页面路由回退 index.html；缺失静态文件返回真实 404，API 错误不能回退成 HTML。
- [ ] 配置 HTTPS、会话 Cookie、必要代理头；开发代理和线上代理分别核对，前端不写死 localhost 或生产 IP。
- [ ] 先发布兼容旧站的新后端，再发布前端并切换入口；分别保留旧前端发布目录、旧 JAR 和路由配置，记录对应版本。
- [ ] HTML 使用可更新缓存策略，带内容哈希资源可长缓存；保留一段过渡期旧资源以避免已打开页面加载失败。
- [ ] 验收后清理旧 IndexController、无引用模板和相应 Thymeleaf 依赖；先确认无代码仍依赖它们，清理可作为独立小任务。

**验收：** 直接打开/刷新 /blog/:id 正常；前进后退、登录过期、API 404、资源 404 正确；页面与 API 都走 HTTPS；前端单独发布无需重启 Java；模拟切回上一版发布目录可恢复页面。服务器配置修改、重启或真实切流量必须在具体环境和影响范围明确后再执行。

阶段依赖：0 → 1 → 2 → 3 → 4 → 5 → 6。阶段 1～2 可作为浏览版交付，但只能开放已完成入口；如需提前上线浏览版，应先执行阶段 6 中适用的部署和回滚验收，不宣称整站迁移完成。

## 8. 首批执行边界与待确认事项

建议第一次实施只推进阶段 0 和阶段 1，交付浏览主线及可核验的数据契约。其余模块按本计划继续，避免一次性重写整个网站。

| 待确认项 | 影响范围 | 未确认时如何处理 |
|---|---|---|
| 实际外键、关联表、type_count 定义 | DTO 组装、分类筛选、文章保存 | 不猜字段或生成表；先读结构 |
| 密码算法、管理员角色含义 | 登录与管理鉴权 | 不默认明文/MD5，也不默认所有用户都是管理员 |
| 草稿公开隔离、计数不改更新时间 | 公开详情、归档、列表顺序 | 作为明确行为修正提交审阅，不静默改变 |
| 标题/摘要/正文搜索范围 | 搜索实现和性能 | 尚未确认前不实现搜索 |
| 分类/标签/文章/评论/留言删除规则 | 所有 DELETE 接口 | 保留删除待办，不擅自级联或改用逻辑删除 |
| 回复层级、分页粒度、审核与防滥用 | 评论/留言公开写接口 | 先完成无歧义的其他模块 |
| 新增依赖、数据库验证写入授权 | 环境准备及写接口验证 | 缺环境时报告，未经授权不执行 |
| 域名、服务器、现有服务和发布路径 | 正式部署 | 先形成产物与配置草案，暂不执行上线 |

## 9. 验证与审阅重点

- API 返回不是 HTML：包括未登录、参数错误、无记录、内部异常，以及旧异常处理器的影响。
- 数据为空或历史关系缺失：缺作者、无分类、无图片、无公告、阅读量为空时，页面不崩溃；不能为了通过验收自动修复数据库。
- 状态与权限：公开读取不泄露草稿，管理接口不能绕过鉴权，客户端不能伪造作者或管理员。
- 内容与日期：Markdown 展示安全且保留格式；日期展示和按年/日归档一致，不对历史时间擅自转换。
- 发布一致性：前端页面刷新可访问，API/静态资源错误不会被 index.html 吞掉，前后端版本兼容且回滚路径明确。

执行时优先用已有工具、人工接口检查和浏览器联调。Maven 验证使用原有配置并优先离线，不换仓库；npm 构建仅在依赖已就绪或安装获准后执行。不以源码检查、存在 target 目录或页面文件存在代替编译/运行验证。本文没有安排新增测试代码。

## 10. 参考位置

本地源码锚点（阅读路径，不表示修改已经发生）：

- `src/main/java/com/zhj/blog/Controller/IndexController.java`：四个现有页面入口。
- `src/main/java/com/zhj/blog/Service/BlogServiceImp.java`：文章查询、转换、计数及归档。
- `src/main/java/com/zhj/blog/Pojo/Blog.java`：文章字段及关联占位。
- `src/main/java/com/zhj/blog/Mapper/TypeMapper.java`：type_count 查询依赖。
- `src/main/java/com/zhj/blog/interceptor/LoginInterceptor.java`、`WebConfig.java`：现有页面会话检查。
- `src/main/java/com/zhj/blog/handler/ExceptionHandler.java`：当前 HTML 错误响应。
- `src/main/resources/templates/admin/blogs-input.html`：文章编辑字段和保存/发布意图。
- `src/main/resources/templates/message.html`、`friend-links.html`：messageType=0/1 的前端依据。

部署与框架参考（前一轮已核对官方文档）：[Vite 静态部署](https://vite.dev/guide/static-deploy.html)、[Vue Router History 部署](https://router.vuejs.org/guide/essentials/history-mode.html)、[Vue HTML 安全](https://vuejs.org/guide/best-practices/security.html)、[Vite 环境变量](https://vite.dev/guide/env-and-mode.html)。实施时再核对实际选用版本。
