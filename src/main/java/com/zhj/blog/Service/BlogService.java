package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import com.zhj.blog.Dto.blog.BlogDetailDto;
import com.zhj.blog.Dto.blog.BlogQuery;
import com.zhj.blog.Dto.blog.TagCountDto;
import com.zhj.blog.Dto.blog.AdminBlogDto;
import com.zhj.blog.Dto.home.HomeDto;

import java.util.List;
import java.util.Map;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:07
 **/
public interface BlogService extends IService<Blog> {
    PageResult<AdminBlogDto.Item> getAdminBlogPage(AdminBlogDto.Query query);
    AdminBlogDto.Edit getAdminBlogEdit(Long id);
    /** 正文、分类和标签关系在同一事务保存；新文章作者由会话传入。 */
    AdminBlogDto.Saved saveAdminBlog(Long id, AdminBlogDto.Input input, Long authorId);
    /** 删除文章时同时删除其标签关系和全部评论。 */
    void deleteAdminBlog(Long id);

    /** 新 API 的公开分页，page >= 1，1 <= size <= 100。 */
    PageResult<BlogSummaryDto> getPublicBlogPage(int page, int size);

    PageResult<BlogSummaryDto> getPublicBlogPage(BlogQuery query);
    List<HomeDto.TypeCountDto> getPublicTypes();
    List<TagCountDto> getPublicTags();
    List<HomeDto.ArchiveDto> getPublicArchives();

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
