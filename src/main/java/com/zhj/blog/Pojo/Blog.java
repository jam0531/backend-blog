package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/9 23:45
 **/
@TableName("blog_table")
@Data
public class Blog {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    @Lob
    private String content;

    private String blogPic;

    private String flag;

    private Integer views;

    private boolean shareStatement;

    private boolean commentAble;

    private boolean publish;

    private boolean Recommend;

    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT_UPDATE ,value = "update_time") // 在插入或更新时自动填充字段
    private LocalDateTime updateTime;

    // 数据库外键单独映射，关联对象由查询服务组装。
    @TableField("type_id")
    private Long typeId;

    @TableField("user_id")
    private Long userId;

    @ManyToOne
    @TableField(exist = false)
    private Type type;

    @TableField(exist = false)
    @ManyToMany(cascade = {CascadeType.PERSIST})
    private List<Tag> tags = new ArrayList<>();

    @TableField(exist = false)
    @ManyToOne
    private User user;

    @TableField(exist = false)
    @OneToMany(mappedBy = "blog")
    private List<Comment> comments = new ArrayList<>();

    private String description;

    @Transient
    @TableField(exist = false)
    private String tagIds;
}
