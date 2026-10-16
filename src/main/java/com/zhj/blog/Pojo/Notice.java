package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author:zouhuajun
 * @date:2022/1/6 0:26
 **/
@Data
@TableName(value = "notice")
public class Notice {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    private LocalDateTime createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT_UPDATE ,value = "update_time")
    private LocalDateTime updateTime;
}
