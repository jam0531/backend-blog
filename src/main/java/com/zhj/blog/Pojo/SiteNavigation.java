package com.zhj.blog.Pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 菜单配置独立于页面内容；隐藏和删除都不关闭对应页面。 */
@Data
@TableName("site_navigation")
public class SiteNavigation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String pageKey;
    private String menuName;
    private Boolean visible;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
