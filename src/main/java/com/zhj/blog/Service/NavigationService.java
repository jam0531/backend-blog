package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhj.blog.Dto.navigation.NavigationDto.Input;
import com.zhj.blog.Dto.navigation.NavigationPage;
import com.zhj.blog.Mapper.SiteNavigationMapper;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.SiteNavigation;
import com.zhj.blog.handler.ApiStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NavigationService {
    private final SiteNavigationMapper mapper;

    public List<SiteNavigation> list(boolean publicOnly) {
        QueryWrapper<SiteNavigation> query = new QueryWrapper<>();
        if (publicOnly) query.eq("visible", 1).in("page_key", NavigationPage.keys());
        return mapper.selectList(query.orderByAsc("sort_order", "id"));
    }

    @Transactional
    public SiteNavigation save(Long id, Input input) {
        if (NavigationPage.find(input.getPageKey()) == null) {
            throw new IllegalArgumentException("不支持的页面");
        }
        SiteNavigation row = new SiteNavigation();
        row.setId(id);
        row.setPageKey(input.getPageKey());
        row.setMenuName(input.getMenuName().trim());
        row.setVisible(input.getVisible());
        row.setSortOrder(input.getSortOrder().intValueExact());
        row.setUpdateTime(LocalDateTime.now());
        try {
            if (id == null) {
                row.setCreateTime(row.getUpdateTime());
                mapper.insert(row);
            } else if (mapper.updateById(row) == 0) {
                throw new NotFoundException("菜单不存在");
            }
        } catch (DuplicateKeyException exception) {
            // 唯一索引覆盖并发新增/改绑同一页面，不仅依赖保存前查询。
            throw new ApiStatusException(HttpStatus.CONFLICT, "该页面已配置菜单，请编辑现有菜单");
        }
        return mapper.selectById(row.getId());
    }

    @Transactional
    public void delete(Long id) {
        // 只删除导航配置，不触碰页面内容或任何业务关联。
        if (mapper.deleteById(id) == 0) throw new NotFoundException("菜单不存在");
    }
}
