package com.zhj.blog.Controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto.*;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Friendlink;
import com.zhj.blog.Pojo.Picture;
import com.zhj.blog.Service.FriendlinkService;
import com.zhj.blog.Service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ResourceApiController {
    private final PictureService pictures;
    private final FriendlinkService friends;

    @GetMapping("/pictures")
    public ApiResponse<PageResult<PictureItem>> pictures(@Valid @ModelAttribute PictureQuery query) {
        return ApiResponse.success(pictures.getPicturePage(query));
    }

    @GetMapping("/pictures/{id}")
    public ApiResponse<PictureItem> picture(@PathVariable @Min(1) Long id) {
        Picture item = pictures.getById(id);
        if (item == null) throw new NotFoundException("图片不存在");
        return ApiResponse.success(PictureItem.from(item));
    }

    @GetMapping("/picture-archives")
    public ApiResponse<List<PictureArchive>> archives() { return ApiResponse.success(pictures.getArchives()); }

    @GetMapping("/friend-links")
    public ApiResponse<List<FriendItem>> friends() {
        return ApiResponse.success(friends.list(new QueryWrapper<Friendlink>().orderByDesc("time", "id"))
                .stream().map(FriendItem::from).collect(Collectors.toList()));
    }
}
