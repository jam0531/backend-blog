# 认证和文章管理 API

沿用 `{code,message,data}` 及 HTTP 状态，所有业务 ID 返回十进制字符串。
后台路由由服务端验证管理员身份；页面路由检查只用于引导登录。

## 登录与会话

| 请求 | 内容 |
|---|---|
| GET `/api/auth/csrf` | 获取当前 Session 的 `token` |
| POST `/api/auth/login` | `username`、`password`；成功返回 `id`、`aliasname`、`avatar` |
| GET `/api/auth/me` | 当前管理员摘要；未登录 401，无权限 403 |
| POST `/api/auth/logout` | 销毁当前会话 |

所有非 GET/HEAD/OPTIONS 的 `/api/**` 请求携带 `X-CSRF-Token`，缺失或不匹配返回 403。
登录成功轮换 Session ID，并废弃旧 CSRF token；客户端重新获取 token 后再写入。
会话仅保存用户 ID，每次管理请求重查用户资格；会话 Cookie 为 HttpOnly、SameSite=Lax。
响应不缓存，错误不返回登录 HTML、密码、SQL 或堆栈。

兼容现有库的无盐 MD5 密码及 `type=1` 管理员约定，不提供注册或密码重置接口。
本地账号修改属于用户单独授权的运维操作，不在仓库配置中记录凭据。

## 文章

| 请求 | 用途 |
|---|---|
| GET `/api/admin/blogs` | `page`、`size`、`keyword`、`typeId`、`publish`、`recommend`；包含草稿与已发布文章 |
| GET `/api/admin/blogs/{id}` | 可编辑字段及 `id`，正文为 `contentMarkdown`；不增加阅读量 |
| POST `/api/admin/blogs` | 新增，ASSIGN_ID 生成主键，作者取当前管理员 |
| PUT `/api/admin/blogs/{id}` | 更新指定文章的完整可编辑字段 |
| DELETE `/api/admin/blogs/{id}` | 事务内删除文章、标签关联及其全部评论 |
| POST `/api/admin/blogs/preview` | 输入 `contentMarkdown`，返回安全渲染的 `contentHtml`；不保存或计数 |

新增和更新字段：

- `title`：必填，最大 255；`description`：必填，最大 200，沿用旧编辑表单限制。
- `blogPic`：必填，最大 255；HTTP(S) 图片地址或以单个 `/` 开头的站内路径。
- `flag`：`原创`、`转载`、`翻译`；`contentMarkdown`：非空原文。
- `typeId`：有效分类 ID；`tagIds`：有效标签 ID 数组，可为空数组。
- `publish`、`recommend`、`shareStatement`、`commentAble`：必填布尔值。

保存响应为 `{id,publish}`。标题、摘要和封面地址去除两端空白，正文不裁剪。
不接收作者、阅读量、创建时间等服务端字段；更新 SQL 不覆盖这些字段。
分类/标签检查、正文保存和关系替换在同一事务中，重复标签去重，空数组清空关联。
分类和标签通过行锁协调保存与删除；关联不存在返回 400，文章不存在返回 404。
公开查询始终只返回 `publish=true` 的文章，撤回草稿后公开详情返回 404。

## 分类与标签

`/api/admin/types` 与 `/api/admin/tags` 分别提供 GET 分页、GET `/{id}`、POST、PUT `/{id}`、DELETE `/{id}`。
输入分别为 `typeName`、`tagName`，必填且最大 255。列表按 ID 倒序，分页从 1 开始、size 最大 100。
被文章使用的分类或标签拒绝删除，返回 409。前端编辑选项遍历管理列表分页，包含尚未被文章使用的条目。

## 本地验收范围（2026-10-16）

已验证成功登录和 Session 轮换、旧 CSRF 失效、退出后 401；错误管理路径未登录返回 JSON 401。
授权的临时数据完成页面新增分类/标签、创建草稿、发布、编辑与清空标签，验证大整数 ID 字符串传递。
接口验证关联去重、非法分类回滚、服务端字段保护、使用中的关联删除拒绝，以及 Markdown HTML 转义。
全部临时主记录与关联已删除；相关五表数据指纹与操作前一致，连接恢复只读。

尚未实际验证非管理员账号、会话自然超时、带评论的删除级联及阅读量写入。
没有新增测试文件，构建使用本地已有依赖。
