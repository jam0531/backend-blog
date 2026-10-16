-- Reference only: do not execute unless restoration is explicitly requested.
-- Before restoring, check remaining constraints and orphan references.
-- ADD CONSTRAINT can fail if related records were deleted or invalid IDs added.
SET SESSION lock_wait_timeout = 5;

ALTER TABLE `blog`.`blog_table`
    ADD CONSTRAINT `FKmmustfwv66xux1hckl6rl3t7k`
        FOREIGN KEY (`user_id`) REFERENCES `blog`.`user` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT `FKq8sl8c8b0m12oy74mh7e35i71`
        FOREIGN KEY (`type_id`) REFERENCES `blog`.`type` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `blog`.`blog_table_tags`
    ADD CONSTRAINT `FK754l9pre0t2ogrf0q2qd280tj`
        FOREIGN KEY (`blogs_id`) REFERENCES `blog`.`blog_table` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT `FKlis49yawe8vso5gao0yxkyjf1`
        FOREIGN KEY (`tags_id`) REFERENCES `blog`.`tag` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `blog`.`comment`
    ADD CONSTRAINT `FKhvh0e2ybgg16bpu229a5teje7`
        FOREIGN KEY (`parent_comment_id`) REFERENCES `blog`.`comment` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT `FKrgos7cn4fnlws46dmb8u6mld8`
        FOREIGN KEY (`blog_id`) REFERENCES `blog`.`blog_table` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `blog`.`message`
    ADD CONSTRAINT `FKt4gjpvxlwdkb0tonr9pd5ftw1`
        FOREIGN KEY (`parent_message_id`) REFERENCES `blog`.`message` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
