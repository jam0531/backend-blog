package com.zhj.blog.Utils;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.*;


public class MarkdownUtils {

    /** API 正文与后台预览共用安全渲染，数据库仍保存 Markdown 原文。 */
    public static String renderPublicContent(String markdown) {
        return renderMarkdown(markdown, extendedSyntax(), false);
    }

    /**
     * markdown格式转换成HTML格式
     * @param markdown
     * @return
     */
    public static String markdownToHtml(String markdown) {
        return renderMarkdown(markdown, Collections.emptyList(), false);
    }

    /**
     * 增加扩展[标题锚点，表格生成]
     * Markdown转换成HTML
     * @param markdown
     * @return
     */
    public static String markdownToHtmlExtensions(String markdown) {
        return renderMarkdown(markdown, extendedSyntax(), true);
    }

    private static List<Extension> extendedSyntax() {
        return Arrays.asList(TablesExtension.create(), HeadingAnchorExtension.create());
    }

    /** 新旧入口使用相同的 HTML/链接安全规则，旧模板保留 Semantic UI 表格样式。 */
    private static String renderMarkdown(String markdown, List<Extension> extensions, boolean legacyTableStyle) {
        Node document = Parser.builder().extensions(extensions).build().parse(markdown == null ? "" : markdown);
        return HtmlRenderer.builder().extensions(extensions).escapeHtml(true).sanitizeUrls(true)
                .attributeProviderFactory(context -> (node, tagName, attributes) -> {
                    if (node instanceof Link && !extensions.isEmpty()) {
                        attributes.put("target", "_blank");
                        attributes.put("rel", "noopener noreferrer");
                    }
                    if (legacyTableStyle && node instanceof TableBlock) {
                        attributes.put("class", "ui celled table");
                    }
                }).build().render(document);
    }


    public static void main(String[] args) {
        String table = "| hello | hi   | 哈哈哈   |\n" +
                "| ----- | ---- | ----- |\n" +
                "| 斯维尔多  | 士大夫  | f啊    |\n" +
                "| 阿什顿发  | 非固定杆 | 撒阿什顿发 |\n" +
                "\n";
        String a = "[imCoding 爱编程](http://www.lirenmi.cn)";
        System.out.println(markdownToHtmlExtensions(a));
    }
}
