package com.zhj.blog.Dto.catalog;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public final class CatalogDto {
    private CatalogDto() { }

    @Getter @AllArgsConstructor
    public static class TypeItem {
        private final String id;
        private final String typeName;
    }
    @Getter @AllArgsConstructor
    public static class TagItem {
        private final String id;
        private final String tagName;
    }
    @Data
    public static class TypeInput {
        @NotBlank @Size(max = 255)
        private String typeName;
    }
    @Data
    public static class TagInput {
        @NotBlank @Size(max = 255)
        private String tagName;
    }
}
