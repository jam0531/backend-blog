package com.zhj.blog.Controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageQuery;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto.*;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Notice;
import com.zhj.blog.Service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@Validated
public class AdminNoticeApiController {
    private final NoticeService notices;

    @GetMapping
    public ApiResponse<PageResult<NoticeItem>> list(@Valid @ModelAttribute PageQuery query) {
        Page<Notice> page = notices.page(new Page<>(query.getPage(), query.getSize()), new QueryWrapper<Notice>().orderByDesc("create_time", "id"));
        return ApiResponse.success(new PageResult<>(page.getRecords().stream().map(NoticeItem::from).collect(Collectors.toList()), page.getTotal(), query.getPage(), query.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeItem> detail(@PathVariable @Min(1) Long id) { return ApiResponse.success(NoticeItem.from(require(id))); }

    @PostMapping
    public ApiResponse<NoticeItem> create(@Valid @RequestBody NoticeInput input) {
        Notice notice = new Notice(); notice.setContent(input.getContent().trim());
        notice.setCreateTime(LocalDateTime.now()); notice.setUpdateTime(notice.getCreateTime());
        notices.save(notice);
        return ApiResponse.success(NoticeItem.from(notice));
    }

    @PutMapping("/{id}")
    public ApiResponse<NoticeItem> update(@PathVariable @Min(1) Long id, @Valid @RequestBody NoticeInput input) {
        Notice notice = new Notice(); notice.setId(id); notice.setContent(input.getContent().trim());
        if (!notices.updateById(notice)) throw new NotFoundException("公告不存在");
        return ApiResponse.success(NoticeItem.from(require(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        if (!notices.removeById(id)) throw new NotFoundException("公告不存在");
        return ApiResponse.success(null);
    }

    private Notice require(Long id) {
        Notice notice = notices.getById(id);
        if (notice == null) throw new NotFoundException("公告不存在");
        return notice;
    }
}
