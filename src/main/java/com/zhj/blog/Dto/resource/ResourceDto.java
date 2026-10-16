package com.zhj.blog.Dto.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhj.blog.Dto.PageQuery;
import com.zhj.blog.Pojo.Friendlink;
import com.zhj.blog.Pojo.Notice;
import com.zhj.blog.Pojo.Picture;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 内容资源的输入/输出白名单；保留既有字段拼写，不直接序列化实体。 */
public final class ResourceDto {
    private ResourceDto() {}
    private static final String IMAGE_URL = "(?i)^(https?://[^\\s]+|/(?!/)[^\\s]*)$";
    private static final String WEB_URL = "(?i)^https?://[^\\s]+$";

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PictureQuery extends PageQuery {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
    }

    @Data
    public static class PictureInput {
        @Size(max = 255) private String picName;
        @NotBlank @Size(max = 255) @Pattern(regexp = IMAGE_URL) private String piclink;
        @Size(max = 255) private String picDescription;
    }

    @Data
    public static class PictureItem {
        private String id;
        private String picName;
        private String piclink;
        private String picDescription;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime createTime;
        public static PictureItem from(Picture picture) {
            PictureItem item = new PictureItem();
            item.id = picture.getId().toString(); item.picName = picture.getPicName();
            item.piclink = picture.getPiclink(); item.picDescription = picture.getPicDescription();
            item.createTime = picture.getCreateTime();
            return item;
        }
    }

    @Data
    public static class PictureArchive {
        private String date;
        private long pictureCount;
    }

    @Data
    public static class FriendInput {
        @NotBlank @Size(max = 255) private String friendlinkName;
        @NotBlank @Size(max = 255) @Pattern(regexp = WEB_URL) private String friendlinkAddress;
        @NotBlank @Size(max = 255) @Pattern(regexp = IMAGE_URL) private String friendlinkAvater;
        @Size(max = 255) private String friendlinkDescription;
    }

    @Data
    public static class FriendItem {
        private String id;
        private String friendlinkName;
        private String friendlinkAddress;
        private String friendlinkAvater;
        private String friendlinkDescription;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime createTime;
        public static FriendItem from(Friendlink friend) {
            FriendItem item = new FriendItem();
            item.id = friend.getId().toString(); item.friendlinkName = friend.getFriendlinkName();
            item.friendlinkAddress = friend.getFriendlinkAddress(); item.friendlinkAvater = friend.getFriendlinkAvater();
            item.friendlinkDescription = friend.getFriendlinkDescription(); item.createTime = friend.getCreateTime();
            return item;
        }
    }

    @Data
    public static class NoticeInput {
        @NotBlank @Size(max = 255) private String content;
    }

    @Data
    public static class NoticeItem {
        private String id;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime updateTime;
        public static NoticeItem from(Notice notice) {
            NoticeItem item = new NoticeItem();
            item.id = notice.getId().toString(); item.content = notice.getContent();
            item.createTime = notice.getCreateTime(); item.updateTime = notice.getUpdateTime();
            return item;
        }
    }
}
