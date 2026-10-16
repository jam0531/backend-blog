package com.zhj.blog.Controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageQuery;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.catalog.CatalogDto.*;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Type;
import com.zhj.blog.Pojo.Tag;
import com.zhj.blog.Service.TypeService;
import com.zhj.blog.Service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminCatalogApiController {
    private final TypeService types;
    private final TagService tags;

    @GetMapping("/types")
    public ApiResponse<PageResult<TypeItem>> types(@Valid @ModelAttribute PageQuery query) {
        Page<Type> page = types.page(new Page<>(query.getPage(), query.getSize()), new QueryWrapper<Type>().orderByDesc("id"));
        return ApiResponse.success(new PageResult<>(page.getRecords().stream().map(t -> new TypeItem(t.getId().toString(), t.getTypeName())).collect(Collectors.toList()), page.getTotal(), query.getPage(), query.getSize()));
    }

    @GetMapping("/types/{id}")
    public ApiResponse<TypeItem> type(@PathVariable @Min(1) Long id) {
        Type item = types.getById(id);
        if (item == null) throw new NotFoundException("分类不存在");
        return ApiResponse.success(new TypeItem(item.getId().toString(), item.getTypeName()));
    }

    @PostMapping("/types")
    public ApiResponse<TypeItem> createType(@Valid @RequestBody TypeInput input) {
        Type item = new Type();
        item.setTypeName(input.getTypeName().trim());
        types.save(item);
        return ApiResponse.success(new TypeItem(item.getId().toString(), item.getTypeName()));
    }

    @PutMapping("/types/{id}")
    public ApiResponse<TypeItem> updateType(@PathVariable @Min(1) Long id, @Valid @RequestBody TypeInput input) {
        Type item = new Type();
        item.setId(id); item.setTypeName(input.getTypeName().trim());
        if (!types.updateById(item)) throw new NotFoundException("分类不存在");
        return ApiResponse.success(new TypeItem(id.toString(), item.getTypeName()));
    }

    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteType(@PathVariable @Min(1) Long id) {
        types.deleteForAdmin(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/tags")
    public ApiResponse<PageResult<TagItem>> tags(@Valid @ModelAttribute PageQuery query) {
        Page<Tag> page = tags.page(new Page<>(query.getPage(), query.getSize()), new QueryWrapper<Tag>().orderByDesc("id"));
        return ApiResponse.success(new PageResult<>(page.getRecords().stream().map(t -> new TagItem(t.getId().toString(), t.getTagName())).collect(Collectors.toList()), page.getTotal(), query.getPage(), query.getSize()));
    }

    @GetMapping("/tags/{id}")
    public ApiResponse<TagItem> tag(@PathVariable @Min(1) Long id) {
        Tag item = tags.getById(id);
        if (item == null) throw new NotFoundException("标签不存在");
        return ApiResponse.success(new TagItem(id.toString(), item.getTagName()));
    }

    @PostMapping("/tags")
    public ApiResponse<TagItem> createTag(@Valid @RequestBody TagInput input) {
        Tag item = new Tag(); item.setTagName(input.getTagName().trim());
        tags.save(item);
        return ApiResponse.success(new TagItem(item.getId().toString(), item.getTagName()));
    }

    @PutMapping("/tags/{id}")
    public ApiResponse<TagItem> updateTag(@PathVariable @Min(1) Long id, @Valid @RequestBody TagInput input) {
        Tag item = new Tag(); item.setId(id); item.setTagName(input.getTagName().trim());
        if (!tags.updateById(item)) throw new NotFoundException("标签不存在");
        return ApiResponse.success(new TagItem(id.toString(), item.getTagName()));
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable @Min(1) Long id) {
        tags.deleteForAdmin(id);
        return ApiResponse.success(null);
    }
}
