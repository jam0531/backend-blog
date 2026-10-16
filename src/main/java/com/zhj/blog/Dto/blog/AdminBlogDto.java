package com.zhj.blog.Dto.blog;

import com.zhj.blog.Dto.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.util.List;

/** 后台文章白名单；输入不接受作者、阅读量、主键或创建时间。 */
public final class AdminBlogDto {
    private AdminBlogDto() {}

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Query extends PageQuery {
        @Size(max = 255)
        private String keyword;
        @Min(1)
        private Long typeId;
        private Boolean publish;
        private Boolean recommend;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends BlogSummaryDto {
        private boolean publish;
        private boolean recommend;
    }

    @Data
    public static class Input {
        @NotBlank @Size(max = 255)
        private String title;
        // 保留旧编辑表单的 200 字摘要限制。
        @NotBlank @Size(max = 200)
        private String description;
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "(?i)^(https?://[^\\s]+|/(?!/)[^\\s]*)$")
        private String blogPic;
        @NotBlank @Pattern(regexp = "原创|转载|翻译")
        private String flag;
        @NotBlank
        private String contentMarkdown;
        @NotNull @Min(1)
        private Long typeId;
        @NotNull
        private List<@NotNull @Min(1) Long> tagIds;
        @NotNull
        private Boolean publish;
        @NotNull
        private Boolean recommend;
        @NotNull
        private Boolean shareStatement;
        @NotNull
        private Boolean commentAble;
    }

    @Data
    public static class Edit {
        private String id;
        private String title;
        private String description;
        private String blogPic;
        private String flag;
        private String contentMarkdown;
        private String typeId;
        private List<String> tagIds;
        private boolean publish;
        private boolean recommend;
        private boolean shareStatement;
        private boolean commentAble;
    }

    @Data
    @AllArgsConstructor
    public static class Saved {
        private String id;
        private boolean publish;
    }

    @Data
    public static class PreviewInput {
        @NotNull
        private String contentMarkdown;
    }
}
