# 导航管理

仅管理公开导航“更多”中的五个已有页面：`album`、`messages`、`friend-links`、`about`、`about-web`。首页、分类、归档、搜索固定保留。页面路径由服务端允许名单和前端命名路由决定，不能通过菜单表生成新页面或配置后台地址。

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/navigation` | 可显示的菜单，按排序值、ID 升序，`Cache-Control: no-store` |
| GET | `/api/admin/navigation` | 全部菜单配置 |
| GET | `/api/admin/navigation/pages` | 可绑定的页面名称、标识、路径 |
| POST | `/api/admin/navigation` | 新增配置 |
| PUT | `/api/admin/navigation/{id}` | 修改配置 |
| DELETE | `/api/admin/navigation/{id}` | 仅删除菜单配置 |

管理接口复用管理员 Session 和写请求的 `X-CSRF-Token`。响应沿用 `ApiResponse`；所有 ID 返回字符串，新增使用 `ASSIGN_ID`。未登录 401，CSRF 不匹配 403，参数非法 400，不存在 404，重复页面 409。

新增/修改只接受可编辑字段：

```json
{"pageKey":"album","menuName":"相册","visible":true,"sortOrder":10}
```

`pageKey` 必须在允许名单内且不能重复；名称不能为空，最多 64 个字符，保存时去首尾空格；`visible` 必填；排序必填且为 0～9999 的整数。ID、时间和实际路径不由客户端指定。公开响应字段为 `id/pageKey/menuName/path`，管理响应另有 `visible/sortOrder/createTime/updateTime`。

隐藏或删除只移除菜单入口，原 URL 和页面内容仍可访问。全部隐藏或删除后，“更多”入口不显示。公开页面首次读取失败时不显示该菜单；已有成功结果则保留当前内存中的结果。后台保存后及重新进入公开布局时刷新；其他已打开标签页需刷新，不做实时推送。

## 首次发布

1. 确认目标数据库及授权后，手工执行 `docs/database/2026-10-16-navigation/01-create-table.sql`，再执行 `02-initialize.sql`。仅新建 `site_navigation`，无外键、不改既有表。初始化 ID 1～5 仅为种子，名称/排序为相册 10、留言 20、友链 30、关于我 40、关于本站 50，均显示。
2. 发布后端，检查公开接口以及管理员列表，再发布前端。
3. 管理地址为 `/admin/navigation`，公开页面不提供后台入口。只读连接模式下不能保存配置，正式管理需使用具有新表写权限的数据库连接。

建表不使用 `IF NOT EXISTS`，已有同名表时应停止核对。初始化不覆盖已有数据，不应重复执行。MySQL DDL 隐式提交，不能用普通事务撤销建表；回退应用版本时可保留新表，删除新表必须另行确认。
