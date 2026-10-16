package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author:zouhuajun
 * @date:2021/12/18 20:45
 **/
@TableName("picture")
@Data
public class Picture {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String picName;

    @NotEmpty(message = "图片地址不能为空")
    private String piclink;

    private String picDescription;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "create_time")
    private LocalDateTime createTime;
}
