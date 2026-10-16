package com.zhj.blog.Dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
public class PageQuery {
    @Min(1)
    private int page = 1;
    @Min(1) @Max(100)
    private int size = 10;
}
