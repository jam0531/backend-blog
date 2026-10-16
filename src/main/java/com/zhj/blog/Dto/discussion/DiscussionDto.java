package com.zhj.blog.Dto.discussion;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhj.blog.Dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DiscussionDto {
    private DiscussionDto() { }

    @Data
    public static class ReplyInput {
        @NotBlank @Size(max = 5000) private String content;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class Input extends ReplyInput {
        @NotBlank @Size(max = 255) private String nickname;
        @NotBlank @Email @Size(max = 255) private String email;
        @Min(1) private Long parentId;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class MessageInput extends Input {
        @NotNull @Min(0) @Max(1) private Integer messageType = 0;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class MessageQuery extends PageQuery {
        @NotNull @Min(0) @Max(1) private Integer messageType = 0;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class AdminQuery extends PageQuery {
        @Min(1) private Long blogId;
        @Min(0) @Max(1) private Integer messageType;
    }

    /** 回复平铺在所属根下；parentId 和 replyTo 保留实际回复对象，不递归序列化实体。 */
    @Data
    public static class Item {
        private String id;
        private String blogId;
        private Integer messageType;
        private String parentId;
        private String nickname;
        private String content;
        private String avatar;
        private boolean admin;
        private String replyTo;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime createTime;
        private List<Item> replies = new ArrayList<>();
    }

    /** 邮箱仅出现在鉴权后的管理列表，不能用作公开响应类型。 */
    @Data @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminItem extends Item {
        private String email;
        private String blogTitle;
    }
}
