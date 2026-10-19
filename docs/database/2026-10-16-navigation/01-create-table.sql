-- 仅在用户确认后，连接本地 blog 数据库手工执行。
-- 不使用 IF NOT EXISTS：若表已存在应停止检查，不能覆盖或重用未知数据。
-- MySQL DDL 隐式提交；本文件只新建此表，不修改任何现有业务表。
CREATE TABLE `site_navigation` (
  `id` BIGINT NOT NULL,
  `page_key` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `menu_name` VARCHAR(64) NOT NULL,
  `visible` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_navigation_page` (`page_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公开更多菜单配置，无页面内容和数据库外键';
