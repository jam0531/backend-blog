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
 * @date:2022/1/12 22:30
 **/
@TableName(value = "message")
@Data
public class Message {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String nickname;
    private String email;
    private String content;
    private String avatar;
//  区分留言和友链的信息
    private Integer messageType = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    private LocalDateTime createTime;

    // 父级关系通过普通 ID 字段保存，回复集合不参与单表 SQL。
    @TableField("parent_message_id")
    private Long parentMessageId;

    @TableField(exist = false)
    private List<Message> replyMessages = new ArrayList<>();

    @TableField(exist = false)
    @ManyToOne
    private Message parentMessage;

    private boolean adminMessage;
}
