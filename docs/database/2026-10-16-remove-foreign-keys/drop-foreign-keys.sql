-- Scope: blog database only. Keep columns, indexes and existing records.
-- MySQL DDL commits automatically. This script is intended to run once.
SET SESSION lock_wait_timeout = 5;

ALTER TABLE `blog`.`blog_table`
    DROP FOREIGN KEY `FKmmustfwv66xux1hckl6rl3t7k`,
    DROP FOREIGN KEY `FKq8sl8c8b0m12oy74mh7e35i71`;

ALTER TABLE `blog`.`blog_table_tags`
    DROP FOREIGN KEY `FK754l9pre0t2ogrf0q2qd280tj`,
    DROP FOREIGN KEY `FKlis49yawe8vso5gao0yxkyjf1`;

ALTER TABLE `blog`.`comment`
    DROP FOREIGN KEY `FKhvh0e2ybgg16bpu229a5teje7`,
    DROP FOREIGN KEY `FKrgos7cn4fnlws46dmb8u6mld8`;

ALTER TABLE `blog`.`message`
    DROP FOREIGN KEY `FKt4gjpvxlwdkb0tonr9pd5ftw1`;
