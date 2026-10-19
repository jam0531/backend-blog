-- 仅在用户确认且新表为空后执行；不使用 REPLACE/IGNORE，不覆盖已有记录。
-- 固定种子 ID 1～5 仅用于首次初始化，后台新增由 MyBatis-Plus ASSIGN_ID 生成。
START TRANSACTION;
INSERT INTO `site_navigation` (`id`, `page_key`, `menu_name`, `visible`, `sort_order`, `create_time`, `update_time`) VALUES
  (1, 'album', '相册', 1, 10, NOW(), NOW()),
  (2, 'messages', '留言', 1, 20, NOW(), NOW()),
  (3, 'friend-links', '友链', 1, 30, NOW(), NOW()),
  (4, 'about', '关于我', 1, 40, NOW(), NOW()),
  (5, 'about-web', '关于本站', 1, 50, NOW(), NOW());
COMMIT;
