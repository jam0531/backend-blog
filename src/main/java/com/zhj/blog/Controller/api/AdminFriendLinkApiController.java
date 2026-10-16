package com.zhj.blog.Controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageQuery;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto.*;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Friendlink;
import com.zhj.blog.Service.FriendlinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/friend-links")
@RequiredArgsConstructor
@Validated
public class AdminFriendLinkApiController {
    private final FriendlinkService friends;

    @GetMapping
    public ApiResponse<PageResult<FriendItem>> list(@Valid @ModelAttribute PageQuery query) {
        Page<Friendlink> page = friends.page(new Page<>(query.getPage(), query.getSize()), new QueryWrapper<Friendlink>().orderByDesc("time", "id"));
        return ApiResponse.success(new PageResult<>(page.getRecords().stream().map(FriendItem::from).collect(Collectors.toList()), page.getTotal(), query.getPage(), query.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FriendItem> detail(@PathVariable @Min(1) Long id) { return ApiResponse.success(FriendItem.from(require(id))); }

    @PostMapping
    public ApiResponse<FriendItem> create(@Valid @RequestBody FriendInput input) {
        Friendlink friend = editable(input); friends.save(friend);
        return ApiResponse.success(FriendItem.from(friend));
    }

    @PutMapping("/{id}")
    public ApiResponse<FriendItem> update(@PathVariable @Min(1) Long id, @Valid @RequestBody FriendInput input) {
        Friendlink friend = editable(input); friend.setId(id);
        if (!friends.updateById(friend)) throw new NotFoundException("友链不存在");
        return ApiResponse.success(FriendItem.from(require(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        if (!friends.removeById(id)) throw new NotFoundException("友链不存在");
        return ApiResponse.success(null);
    }

    private Friendlink require(Long id) {
        Friendlink friend = friends.getById(id);
        if (friend == null) throw new NotFoundException("友链不存在");
        return friend;
    }

    private Friendlink editable(FriendInput input) {
        Friendlink friend = new Friendlink();
        friend.setFriendlinkName(input.getFriendlinkName().trim());
        friend.setFriendlinkAddress(input.getFriendlinkAddress().trim());
        friend.setFriendlinkAvater(input.getFriendlinkAvater().trim());
        friend.setFriendlinkDescription(input.getFriendlinkDescription() == null ? "" : input.getFriendlinkDescription().trim());
        return friend;
    }
}
