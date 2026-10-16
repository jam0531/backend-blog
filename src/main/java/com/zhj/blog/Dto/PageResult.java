package com.zhj.blog.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/** page 从 1 开始；越界页保留请求页码，records 为空。 */
@Getter
@AllArgsConstructor
public class PageResult<T> {
    private final List<T> records;
    private final long total;
    private final long page;
    private final long size;
}
