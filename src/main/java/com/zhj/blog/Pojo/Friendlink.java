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
 * @date:2021/12/16 0:03
 **/

@TableName(value = "friendlink")
@Data
public class Friendlink {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String friendlinkName;

    @NotEmpty(message = "地址不能为空")
    private String friendlinkAddress;

    private String friendlinkAvater;

    private String friendlinkDescription;

    @Temporal(TemporalType.TIMESTAMP)
    @TableField(fill = FieldFill.INSERT ,value = "time")
    private LocalDateTime createTime;

}
