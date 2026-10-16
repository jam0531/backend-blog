package com.zhj.blog.Pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:zouhuajun
 * @date:2021/12/13 13:28
 **/
@Data
@TableName(value = "type")
public class Type {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @NotEmpty(message = "分类名称不能为空")
    private String typeName;

    @TableField(exist = false)
    @OneToMany(mappedBy = "type")
    private List<Blog> blogs = new ArrayList<>();
}
