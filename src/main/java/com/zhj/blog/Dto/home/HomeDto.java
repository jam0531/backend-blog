package com.zhj.blog.Dto.home;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** 首页栏目聚合数据；公告不存在时返回 null，其余集合返回空数组。 */
@Data
public class HomeDto {
    private NoticeDto notice;
    private List<BlogSummaryDto> recommendedBlogs;
    private List<TypeCountDto> topTypes;
    private StatsDto stats;
    private List<ArchiveDto> archives;

    /** 独立背景接口使用，不随首页栏目聚合返回。 */
    @Data
    @AllArgsConstructor
    public static class BackgroundDto {
        private String id;
        private String picName;
        private String piclink;
    }

    @Data
    @AllArgsConstructor
    public static class NoticeDto {
        private String id;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createTime;
    }

    /** 分类、标签只统计已发布文章实际关联且仍存在的记录。 */
    @Data
    public static class StatsDto {
        private long blogCount;
        private long typeCount;
        private long tagCount;
    }

    @Data
    public static class TypeCountDto {
        private String id;
        private String typeName;
        private long blogCount;
    }

    @Data
    public static class ArchiveDto {
        private Integer year;
        private long blogCount;
    }
}
