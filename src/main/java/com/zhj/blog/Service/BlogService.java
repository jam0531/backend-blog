package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import com.zhj.blog.Dto.blog.BlogDetailDto;
import com.zhj.blog.Dto.home.HomeDto;

import java.util.List;
import java.util.Map;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:07
 **/
public interface BlogService extends IService<Blog> {
    /** 新 API 的公开分页，page >= 1，1 <= size <= 100。 */
    PageResult<BlogSummaryDto> getPublicBlogPage(int page, int size);

    /** 公开首页聚合，不查询或更新文章详情。 */
    HomeDto getPublicHome();

    /** 公开详情，未发布文章与不存在的文章统一视为不存在。 */
    BlogDetailDto getPublicBlogDetail(Long id);

    // 获取所有发布的博客
    Page<Blog> PublishedBlogList(Page page);

    // 最新的博客
    List<Blog> BlogTopList(Integer num);

    // 推荐的博客
    List<Blog> RecommendBlogList(Integer size);

    // 获取博客并将内容转换为html格式
    Blog getBlogByIdAndContentConvert(Long id);

    // 博客归档
    Map<String, List<Blog>> archiveBlog();
    Map<String, List<Blog>> archiveBlog(String year);
}
