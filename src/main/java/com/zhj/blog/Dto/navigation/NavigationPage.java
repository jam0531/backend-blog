package com.zhj.blog.Dto.navigation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** 只登记已实现的公开页面，禁止通过管理请求传入任意网址或组件路径。 */
@Getter
@RequiredArgsConstructor
public enum NavigationPage {
    ALBUM("album", "相册", "/album"),
    MESSAGES("messages", "留言", "/messages"),
    FRIEND_LINKS("friend-links", "友链", "/friend-links"),
    ABOUT("about", "关于我", "/about"),
    ABOUT_WEB("about-web", "关于本站", "/about-web");

    private final String pageKey;
    private final String pageName;
    private final String path;

    public static NavigationPage find(String key) {
        for (NavigationPage page : values()) {
            if (page.pageKey.equals(key)) return page;
        }
        return null;
    }

    public static List<String> keys() {
        return Arrays.stream(values()).map(NavigationPage::getPageKey).collect(Collectors.toList());
    }
}
