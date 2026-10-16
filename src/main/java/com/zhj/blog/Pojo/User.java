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
 * @date:2021/12/13 13:34
 **/
@Data
@TableName("user")
public class User {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String aliasname;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private Integer type;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    private LocalDateTime createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT_UPDATE ,value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @OneToMany(mappedBy = "user")
    private List<Blog> blogs = new ArrayList<>();
}
