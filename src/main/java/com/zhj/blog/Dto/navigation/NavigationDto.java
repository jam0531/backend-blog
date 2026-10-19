package com.zhj.blog.Dto.navigation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhj.blog.Pojo.SiteNavigation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class NavigationDto {
    private NavigationDto() {}

    /** ID、创建时间和实际路由均由服务端决定，不属于可编辑字段。 */
    @Data
    public static class Input {
        @NotBlank @Size(max = 32) private String pageKey;
        @NotBlank @Size(max = 64) private String menuName;
        @NotNull private Boolean visible;
        // 保留输入小数精度交给校验，避免 Jackson 将 1.5 静默截成 1。
        @NotNull @Min(0) @Max(9999) @Digits(integer = 4, fraction = 0)
        private BigDecimal sortOrder;
    }

    @Data
    public static class PublicItem {
        private String id;
        private String pageKey;
        private String menuName;
        private String path;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AdminItem extends PublicItem {
        private Boolean visible;
        private Integer sortOrder;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime updateTime;
    }

    public static PublicItem toPublic(SiteNavigation row) {
        PublicItem item = new PublicItem();
        fill(item, row);
        return item;
    }

    public static AdminItem toAdmin(SiteNavigation row) {
        AdminItem item = new AdminItem();
        fill(item, row);
        item.setVisible(row.getVisible());
        item.setSortOrder(row.getSortOrder());
        item.setCreateTime(row.getCreateTime());
        item.setUpdateTime(row.getUpdateTime());
        return item;
    }

    private static void fill(PublicItem item, SiteNavigation row) {
        item.setId(row.getId().toString());
        item.setPageKey(row.getPageKey());
        item.setMenuName(row.getMenuName());
        NavigationPage page = NavigationPage.find(row.getPageKey());
        item.setPath(page == null ? null : page.getPath());
    }
}
