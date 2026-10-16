package com.zhj.blog.Dto.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhj.blog.Pojo.Blog;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 公开摘要白名单，不包含正文、账号凭据及实体反向关联。 */
@Data
public class BlogSummaryDto {
    private String id;
    private String title;
    private String description;
    private String blogPic;
    private String flag;
    private int views;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;
    private AuthorDto author;
    private TypeDto type;
    private List<TagDto> tags;

    public static BlogSummaryDto from(Blog blog) {
        BlogSummaryDto dto = new BlogSummaryDto();
        dto.setId(blog.getId().toString());
        dto.setTitle(blog.getTitle());
        dto.setDescription(blog.getDescription());
        dto.setBlogPic(blog.getBlogPic());
        dto.setFlag(blog.getFlag());
        dto.setViews(blog.getViews() == null ? 0 : blog.getViews());
        dto.setCreateTime(blog.getCreateTime());
        dto.setUpdateTime(blog.getUpdateTime());
        if (blog.getUser() != null) {
            dto.setAuthor(new AuthorDto(blog.getUser().getId().toString(),
                    blog.getUser().getAliasname(), blog.getUser().getAvatar()));
        }
        if (blog.getType() != null) {
            dto.setType(new TypeDto(blog.getType().getId().toString(), blog.getType().getTypeName()));
        }
        dto.setTags(blog.getTags().stream()
                .map(tag -> new TagDto(tag.getId().toString(), tag.getTagName()))
                .collect(Collectors.toList()));
        return dto;
    }

    @Data
    @AllArgsConstructor
    public static class AuthorDto {
        private String id;
        private String aliasname;
        private String avatar;
    }

    @Data
    @AllArgsConstructor
    public static class TypeDto {
        private String id;
        private String typeName;
    }

    @Data
    @AllArgsConstructor
    public static class TagDto {
        private String id;
        private String tagName;
    }
}
