package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:zouhuajun
 * @date:2021/12/13 13:29
 **/
@TableName(value = "tag")
@Data
public class Tag {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String tagName;

    @TableField(exist = false)
    @ManyToMany(mappedBy = "tags")
    private List<Blog> blogs = new ArrayList<>();
}
