package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author:zouhuajun
 * @date:2021/12/13 13:30
 **/
@TableName(value = "comment")
@Data
public class Comment {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String nickname;
    private String email;
    private String content;
    private String avatar;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    private LocalDateTime createTime;

    // 持久化使用外键 ID，对象字段仅用于页面展示。
    @TableField("blog_id")
    private Long blogId;

    @TableField("parent_comment_id")
    private Long parentCommentId;

    @TableField(exist = false)
    @ManyToOne
    private Blog blog;

    @OneToMany(mappedBy = "parentComment")
    @TableField(exist = false)
    private List<Comment> replyComments = new ArrayList<>();

    @TableField(exist = false)
    @ManyToOne
    private Comment parentComment;

    private boolean adminComment;
}
