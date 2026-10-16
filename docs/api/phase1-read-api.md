# 第一阶段公开查询 API

当前实现首页聚合、文章分页及公开详情。旧页面入口保留，登录和业务新增接口尚未实现。本地核验没有执行数据库结构或数据变更。

## 公共契约

- 成功：HTTP 200，`{"code":0,"message":"success","data":...}`。
- 失败：真实 HTTP 状态码，`{"code":400,"message":"请求参数不合法","data":null}` 等；不返回 SQL、堆栈或登录 HTML。
- 所有业务 ID（文章、作者、分类、标签、背景图片、公告）为十进制字符串。Java 实体和数据库类型保持不变。
- 时间格式：`yyyy-MM-dd'T'HH:mm:ss`，不附加时区、不转换历史时间。
- 公开文章查询均限定 `publish = 1`，摘要 DTO 不包含正文；详情仅包含安全渲染的 contentHtml。所有公开 DTO 不包含密码、邮箱或反向关联集合。
- 旧 HTML 页面不适用本 API 契约，其原有查询及详情计数行为未修改。

## GET /api/blogs

参数：`page` 默认 1，范围 1 至 Java int 上限；`size` 默认 10，范围 1 至 100。非整数、溢出或越界参数返回 400。没有新增分类、标签、搜索、年份筛选。

`data` 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| records | 数组 | 文章摘要 |
| total | 数字 | 已发布文章总数 |
| page | 数字 | 请求页码，从 1 开始 |
| size | 数字 | 每页条数 |

按 `update_time DESC, id DESC` 排序；越界页返回空 records 和真实 total，不跳回第一页。

文章摘要字段：`id`、`title`、`description`、`blogPic`、`flag`、`views`、`createTime`、`updateTime`、`author`、`type`、`tags`。

- `author`：`id`、`aliasname`、`avatar`；关联作者缺失时为 null。
- `type`：`id`、`typeName`；关联分类缺失时为 null。
- `tags`：由 `id`、`tagName` 组成的数组，无标签时为 []。
- 数据库 views 为 null 时，展示值为 0，不回写数据库。

## GET /api/home

不需要参数。`data` 字段：

| 字段 | 内容 |
|---|---|
| background | 随机图片的 id、picName、piclink；无图片时为 null |
| notice | 最新公告的 id、content、createTime；无公告时为 null |
| latestBlogs | 最新 5 篇已发布文章摘要，create_time DESC、id DESC |
| recommendedBlogs | 推荐的 6 篇已发布文章摘要，同上排序 |
| topTypes | 最多 5 项：id、typeName、blogCount；已发布文章数量降序，同数量按分类 id 降序 |
| stats | blogCount、typeCount、tagCount |
| archives | year、blogCount 数组，年份降序 |

统计口径：

- blogCount 为所有已发布文章数量。
- typeCount、tagCount 仅统计已发布文章实际关联、且仍存在的分类/标签，使用去重计数。
- topTypes 不包含零篇已发布文章的分类，不依赖旧 type_count 视图。
- archives 按已发布文章 create_time 年份分组，create_time 为空的历史记录不归入年份；因此年份数量之和可能小于 blogCount。
- 统计使用数据库聚合，不加载文章正文；最新和推荐文章共用批量关联查询。
- 本接口不执行阅读量或其他写入。

## 异常处理范围

新 API Controller 使用独立异常处理；旧 IndexController 仍返回原有 HTML 错误页。/api 下未匹配路由、错误请求方法等进入默认错误分发时，返回统一 JSON；请求 Accept 为 text/html 时也不返回错误 HTML。其他页面的默认错误处理保留 Spring Boot 行为。

## GET /api/blogs/{id}

在摘要字段基础上返回 contentHtml、shareStatement、commentAble。ID 必须是正 Long；未发布与不存在统一返回 404。正文原始 HTML 转义，危险链接协议被过滤，数据库中的 Markdown 不变。

默认对已发布文章的 views 原子加一，不更新正文或 update_time。只读联调时显式设置 `--blog.reading-count-enabled=false`，同时使用只读数据源；该开关仅用于不写库的核验，不代表正式环境取消计数。

## 本次验证记录

- 使用原有 Maven settings.xml 和 JDK 8，执行离线 compile，结果 BUILD SUCCESS。
- 没有新增、修改或执行测试代码，也没有连接数据库执行写操作。
- 已通过项目原有 spring-boot:run 插件离线启动，无需补装 dependency 插件，未修改 Maven 配置。
- 首页及文章分页 HTTP 200，当前数据共 10 篇已发布文章，返回 ID 为字符串；非法参数为 JSON 400，未知 API 在 Accept:text/html 下仍为 JSON 404；越界页返回空 records 和真实 total。
- 详情及阅读量逻辑随后新增，仅完成编译，尚未执行写库验证；草稿隔离及缺失关联边界尚未用专门数据运行验证。
