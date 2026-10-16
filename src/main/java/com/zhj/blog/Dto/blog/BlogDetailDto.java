package com.zhj.blog.Dto.blog;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公开详情只输出安全渲染的 HTML，不输出 Markdown 原文及管理字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BlogDetailDto extends BlogSummaryDto {
    private String contentHtml;
    private boolean shareStatement;
    private boolean commentAble;
}
