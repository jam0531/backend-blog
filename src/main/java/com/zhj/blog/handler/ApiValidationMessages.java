package com.zhj.blog.handler;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 只使用服务端字段白名单和约束元数据构造提示，不回显输入值、Java类型或异常文本。 */
final class ApiValidationMessages {
    private static final Map<String, String> LABELS = new HashMap<>();
    static {
        LABELS.put("username", "账号");
        LABELS.put("password", "密码");
        LABELS.put("title", "文章标题");
        LABELS.put("description", "文章摘要");
        LABELS.put("contentMarkdown", "文章正文");
        LABELS.put("blogPic", "封面地址");
        LABELS.put("flag", "文章来源");
        LABELS.put("typeId", "分类 ID");
        LABELS.put("tagId", "标签 ID");
        LABELS.put("tagIds", "标签 ID");
        LABELS.put("id", "记录 ID");
        LABELS.put("blogId", "文章 ID");
        LABELS.put("parentId", "回复对象 ID");
        LABELS.put("excludeId", "排除图片 ID");
        LABELS.put("publish", "发布状态");
        LABELS.put("recommend", "推荐状态");
        LABELS.put("commentAble", "评论开关");
        LABELS.put("shareStatement", "转载声明开关");
        LABELS.put("typeName", "分类名称");
        LABELS.put("tagName", "标签名称");
        LABELS.put("nickname", "昵称");
        LABELS.put("email", "邮箱");
        LABELS.put("content", "内容");
        LABELS.put("messageType", "留言类型");
        LABELS.put("page", "页码");
        LABELS.put("size", "每页条数");
        LABELS.put("keyword", "关键词");
        LABELS.put("year", "年份");
        LABELS.put("date", "日期");
        LABELS.put("picName", "图片名称");
        LABELS.put("piclink", "图片地址");
        LABELS.put("picDescription", "图片描述");
        LABELS.put("friendlinkName", "友链名称");
        LABELS.put("friendlinkAddress", "友链地址");
        LABELS.put("friendlinkAvater", "友链头像地址");
        LABELS.put("friendlinkDescription", "友链描述");
        LABELS.put("pageKey", "关联页面");
        LABELS.put("menuName", "菜单名称");
        LABELS.put("sortOrder", "排序值");
        LABELS.put("visible", "菜单显示状态");
    }

    private ApiValidationMessages() {}

    static String binding(BindingResult result) {
        Map<String, String> messages = new LinkedHashMap<>();
        result.getFieldErrors().stream().sorted(Comparator.comparing(FieldError::getField)
                .thenComparing(error -> required(error.getCode()) ? 0 : 1)
                .thenComparing(error -> String.valueOf(error.getCode()))).forEach(error -> {
            String field = field(error.getField());
            if (error.isBindingFailure()) {
                messages.putIfAbsent(field, invalidType(field));
            } else if (error.contains(ConstraintViolation.class)) {
                ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
                messages.putIfAbsent(field, constraint(field, violation.getConstraintDescriptor()));
            } else {
                messages.putIfAbsent(field, label(field) + "不符合要求");
            }
        });
        return join(messages);
    }

    static String violations(Set<ConstraintViolation<?>> violations) {
        Map<String, String> messages = new LinkedHashMap<>();
        violations.stream().sorted(Comparator.comparing(value -> value.getPropertyPath().toString()))
                .forEach(value -> {
                    String field = field(value.getPropertyPath().toString());
                    messages.putIfAbsent(field, constraint(field, value.getConstraintDescriptor()));
                });
        return join(messages);
    }

    static String label(String path) { return LABELS.getOrDefault(field(path), "请求参数"); }

    static String invalidType(String path) {
        String field = field(path);
        if ("date".equals(field)) return "日期格式不正确，请填写有效的 yyyy-MM-dd 日期";
        if (field.endsWith("Id") || "id".equals(field) || "tagIds".equals(field)
                || "page".equals(field) || "size".equals(field) || "year".equals(field)
                || "messageType".equals(field) || "sortOrder".equals(field)) {
            return label(field) + "必须为有效的整数";
        }
        return label(field) + "格式不正确";
    }

    private static String constraint(String field, ConstraintDescriptor<?> constraint) {
        String code = constraint.getAnnotation().annotationType().getSimpleName();
        Map<String, Object> attributes = constraint.getAttributes();
        String label = label(field);
        if (required(code)) return "请填写" + label;
        switch (code) {
            case "Size":
                int min = ((Number) attributes.get("min")).intValue();
                int max = ((Number) attributes.get("max")).intValue();
                return min == 0 ? label + "不能超过 " + max + " 个字符"
                        : label + "长度须为 " + min + "～" + max + " 个字符";
            case "Min": return label + "不能小于 " + attributes.get("value");
            case "Max": return label + "不能大于 " + attributes.get("value");
            case "Email": return label + "格式不正确";
            case "Digits": return label + "必须为整数，且不能超过 " + attributes.get("integer") + " 位";
            case "Pattern":
                if ("blogPic".equals(field) || "piclink".equals(field) || "friendlinkAvater".equals(field)) {
                    return label + "须为 http/https 地址或以 / 开头的站内路径";
                }
                if ("friendlinkAddress".equals(field)) return "友链地址须以 http:// 或 https:// 开头";
                if ("flag".equals(field)) return "文章来源只能选择原创、转载或翻译";
                return label + "格式不正确";
            default: return label + "不符合要求";
        }
    }

    private static boolean required(String code) {
        return "NotBlank".equals(code) || "NotNull".equals(code) || "NotEmpty".equals(code);
    }

    private static String field(String path) {
        if (path == null) return "";
        String result = "";
        for (String part : path.split("\\.")) {
            String name = part.replaceAll("\\[.*?\\]", "");
            if (LABELS.containsKey(name)) result = name;
        }
        return result;
    }

    private static String join(Map<String, String> messages) {
        return messages.isEmpty() ? "请求参数不合法" : messages.values().stream().distinct().limit(3)
                .collect(java.util.stream.Collectors.joining("；"));
    }
}
