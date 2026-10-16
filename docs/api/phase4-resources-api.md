# 相册、友链与基础内容管理

统一使用 ApiResponse；实体 ID 为字符串，日期时间为站点本地时间，不转换时区。

| 接口 | 用途 |
| --- | --- |
| GET /api/pictures?page=1&size=12&date=2022-04-07 | 相册，date 可选，范围 1000-01-01 至 9999-12-31 |
| GET /api/pictures/{id} | 图片详情 |
| GET /api/picture-archives | 按日期倒序返回 date、pictureCount |
| GET /api/friend-links | 友链列表，按 time、id 倒序 |
| /api/admin/notices | 公告 CRUD |
| /api/admin/pictures | 图片 CRUD |
| /api/admin/friend-links | 友链 CRUD |

三个管理资源均支持 GET 分页列表、GET /{id}、POST 新增、PUT /{id} 修改、DELETE /{id}。
管理端要求管理员 Session；所有写请求要求 X-CSRF-Token。

- 公告仅接收 content，必填，最多 255 字。首页展示 create_time、id 倒序的第一条，修改内容不改创建时间。
- 图片接收 picName、piclink、picDescription，各最多 255 字；仅 piclink 必填，允许 HTTP(S) 或站内绝对路径。填写地址，不上传文件。
- 友链接收 friendlinkName、friendlinkAddress、friendlinkAvater、friendlinkDescription；前三项必填。字段 Avater 保持既有拼写。博客地址限定 HTTP(S)，头像允许 HTTP(S) 或站内路径。
- 创建时间、更新时间和 ID 由服务端控制，更新使用字段白名单。可选描述传空字符串可清空。
- 公开页面对地址再次校验，图片加载失败显示占位，不执行存量记录中的脚本协议。

已在本地授权的三条临时记录上完成增改查删验证并清理，原三表数据指纹一致。未进行生产部署。
