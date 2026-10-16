package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto.*;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Picture;
import com.zhj.blog.Service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/admin/pictures")
@RequiredArgsConstructor
@Validated
public class AdminPictureApiController {
    private final PictureService pictures;

    @GetMapping
    public ApiResponse<PageResult<PictureItem>> list(@Valid @ModelAttribute PictureQuery query) {
        return ApiResponse.success(pictures.getPicturePage(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<PictureItem> detail(@PathVariable @Min(1) Long id) { return ApiResponse.success(PictureItem.from(require(id))); }

    @PostMapping
    public ApiResponse<PictureItem> create(@Valid @RequestBody PictureInput input) {
        Picture picture = editable(input);
        pictures.save(picture);
        return ApiResponse.success(PictureItem.from(picture));
    }

    @PutMapping("/{id}")
    public ApiResponse<PictureItem> update(@PathVariable @Min(1) Long id, @Valid @RequestBody PictureInput input) {
        Picture picture = editable(input); picture.setId(id);
        if (!pictures.updateById(picture)) throw new NotFoundException("图片不存在");
        return ApiResponse.success(PictureItem.from(require(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        if (!pictures.removeById(id)) throw new NotFoundException("图片不存在");
        return ApiResponse.success(null);
    }

    private Picture require(Long id) {
        Picture picture = pictures.getById(id);
        if (picture == null) throw new NotFoundException("图片不存在");
        return picture;
    }

    private Picture editable(PictureInput input) {
        Picture picture = new Picture();
        picture.setPicName(input.getPicName() == null ? "" : input.getPicName().trim());
        picture.setPiclink(input.getPiclink().trim());
        picture.setPicDescription(input.getPicDescription() == null ? "" : input.getPicDescription().trim());
        return picture;
    }
}
