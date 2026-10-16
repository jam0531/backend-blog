package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Dto.BlogTagDto;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import com.zhj.blog.Dto.blog.BlogDetailDto;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.TypeMapper;
import com.zhj.blog.Mapper.UserMapper;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Tag;
import com.zhj.blog.Pojo.Type;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.Pojo.Picture;
import com.zhj.blog.Pojo.Notice;
import com.zhj.blog.Utils.MarkdownUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:12
 **/
@Service
public class BlogServiceImp extends ServiceImpl<BlogMapper, Blog> implements BlogService{
    @Autowired
    private BlogMapper mapper;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private NoticeService noticeService;

    // 只读联调可显式关闭计数；正常运行保留浏览文章时增加阅读量的业务行为。
    @Value("${blog.reading-count-enabled:true}")
    private boolean readingCountEnabled;

    @Override
    @Transactional
    public BlogDetailDto getPublicBlogDetail(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("文章 ID 必须为正整数");
        }
        Blog blog = mapper.selectOne(new QueryWrapper<Blog>().eq("id", id).eq("publish", 1));
        if (blog == null) {
            throw new NotFoundException("文章不存在");
        }
        populateRelations(Collections.singletonList(blog));
        BlogDetailDto dto = new BlogDetailDto();
        BeanUtils.copyProperties(BlogSummaryDto.from(blog), dto);
        dto.setContentHtml(MarkdownUtils.renderPublicContent(blog.getContent()));
        dto.setShareStatement(blog.isShareStatement());
        dto.setCommentAble(blog.isCommentAble());
        if (readingCountEnabled) {
            if (mapper.incrementPublishedViews(id) == 0) {
                throw new NotFoundException("文章不存在");
            }
            dto.setViews(mapper.selectPublishedViews(id));
        }
        return dto;
    }

    @Override
    public PageResult<BlogSummaryDto> getPublicBlogPage(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 必须大于等于 1，size 必须在 1 到 100 之间");
        }
        Page<Blog> result = mapper.selectPage(new Page<>(page, size), publicSummaryQuery()
                .orderByDesc("update_time", "id"));
        return new PageResult<>(toSummaries(result.getRecords()), result.getTotal(), page, size);
    }

    @Override
    public HomeDto getPublicHome() {
        HomeDto dto = new HomeDto();
        Picture picture = pictureService.getRandomPic(1);
        if (picture != null) {
            dto.setBackground(new HomeDto.BackgroundDto(picture.getId().toString(),
                    picture.getPicName(), picture.getPiclink()));
        }
        Notice notice = noticeService.getLatestNotice();
        if (notice != null) {
            dto.setNotice(new HomeDto.NoticeDto(notice.getId().toString(),
                    notice.getContent(), notice.getCreateTime()));
        }
        List<Blog> latest = mapper.selectList(publicSummaryQuery()
                .orderByDesc("create_time", "id").last("LIMIT 5"));
        List<Blog> recommended = mapper.selectList(publicSummaryQuery().eq("recommend", 1)
                .orderByDesc("create_time", "id").last("LIMIT 6"));
        // 两组文章共用一次批量关联查询；不为每篇文章单独查作者、分类和标签。
        List<Blog> blogs = new ArrayList<>(latest);
        blogs.addAll(recommended);
        populateRelations(blogs);
        dto.setLatestBlogs(latest.stream().map(BlogSummaryDto::from).collect(Collectors.toList()));
        dto.setRecommendedBlogs(recommended.stream().map(BlogSummaryDto::from).collect(Collectors.toList()));
        dto.setTopTypes(typeMapper.selectPublishedTopTypes(5));
        dto.setStats(mapper.selectPublishedStats());
        dto.setArchives(mapper.selectPublishedArchives());
        return dto;
    }

    /** 摘要查询不加载正文，固定字段白名单避免客户端控制 SQL 字段。 */
    private QueryWrapper<Blog> publicSummaryQuery() {
        return new QueryWrapper<Blog>().select("id", "title", "description", "blog_pic", "flag",
                "views", "create_time", "update_time", "type_id", "user_id").eq("publish", 1);
    }

    private List<BlogSummaryDto> toSummaries(List<Blog> blogs) {
        populateRelations(blogs);
        return blogs.stream().map(BlogSummaryDto::from).collect(Collectors.toList());
    }

    /* 获取所有发布的博客 */
    @Override
    public Page<Blog> PublishedBlogList(Page page) {
        Page<Blog> blogs = mapper.selectPage(page, new QueryWrapper<Blog>().eq("publish", 1).orderByDesc("update_time"));
        populateRelations(blogs.getRecords());
        return blogs;
    }

    // 最新的博客
    @Override
    public List<Blog> BlogTopList(Integer num) {
        Map<String, Object> params = new HashMap<>();
        params.put("publish", 1);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper();
        queryWrapper.allEq(params).orderByDesc("create_time").last("limit "+num);
        List<Blog> blogs = mapper.selectList(queryWrapper);
        populateRelations(blogs);
        return blogs;
    }

    // 推荐的博客
    @Override
    public List<Blog> RecommendBlogList(Integer size) {
        Map<String, Object> params = new HashMap<>();
        params.put("publish", 1);
        params.put("recommend", 1);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper();
        queryWrapper.allEq(params).orderByDesc("create_time").last("limit "+size);
        List<Blog> blogs = mapper.selectList(queryWrapper);
        populateRelations(blogs);
        return blogs;
    }

    // 获取博客并将内容转换为html格式
    @Override
    public Blog getBlogByIdAndContentConvert(Long id) {
        Blog blog = mapper.selectById(id);
        if (blog == null){
            throw new NotFoundException("该博客不存在");
        }
        populateRelations(Collections.singletonList(blog));
        // 新建一个Blog对象进行内容转换避免修改数据库中的内容
        Blog blog1 = new Blog();
        BeanUtils.copyProperties(blog, blog1);
        String content = blog1.getContent();
        blog1.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
        blog.setViews(blog.getViews()+1);
        mapper.updateById(blog);
        return blog1;
    }

    @Override
    public Map<String, List<Blog>> archiveBlog() {
        List<Blog> blogs = mapper.selectList(new QueryWrapper<Blog>().orderByDesc("create_time "));
        populateRelations(blogs);
        Map<String , List<Blog>> map = new LinkedHashMap<>();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        List<String> yearlist = new ArrayList<>();
        for (Blog blog : blogs){
//            String year = formatter.format(blog.getCreateTime());
            String year = Integer.toString(blog.getCreateTime().getYear());
            if(!yearlist.contains(year)){
                yearlist.add(year);
            }
        }

        for (String year : yearlist){
            List<Blog> blogList = new ArrayList<>();
            for (Blog blog: blogs){
                if (Integer.toString(blog.getCreateTime().getYear()).equals(year)){
                    blogList.add(blog);
                }
            }
            map.put(year, blogList);
        }

        return map;
    }

    @Override
    public Map<String, List<Blog>> archiveBlog(String year) {
        List<Blog> blogs = mapper.selectList(new QueryWrapper<Blog>().orderByDesc("create_time "));
        populateRelations(blogs);
        List<Blog> blogList = new ArrayList<>();
        Map<String , List<Blog>> map = new LinkedHashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        for (Blog blog: blogs){
            if (formatter.format(blog.getCreateTime()).equals(year)){
                blogList.add(blog);
            }
        }
        map.put(year, blogList);
        return map;
    }

    /**
     * MyBatis-Plus 不会根据 JPA 关系注解加载对象，按本次文章集合批量补齐展示数据。
     * 不加载反向文章集合，避免关联对象互相引用。
     */
    private void populateRelations(List<Blog> blogs) {
        if (blogs.isEmpty()) {
            return;
        }
        List<Long> blogIds = new ArrayList<>();
        Set<Long> typeIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (Blog blog : blogs) {
            blogIds.add(blog.getId());
            if (blog.getTypeId() != null) {
                typeIds.add(blog.getTypeId());
            }
            if (blog.getUserId() != null) {
                userIds.add(blog.getUserId());
            }
        }

        Map<Long, Type> types = new HashMap<>();
        if (!typeIds.isEmpty()) {
            for (Type type : typeMapper.selectBatchIds(typeIds)) {
                types.put(type.getId(), type);
            }
        }
        Map<Long, User> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            // 展示作者不需要读取密码、邮箱等账号信息。
            List<User> authors = userMapper.selectList(new QueryWrapper<User>()
                    .select("id", "aliasname", "avatar").in("id", userIds));
            for (User user : authors) {
                users.put(user.getId(), user);
            }
        }
        Map<Long, List<Tag>> tags = new HashMap<>();
        for (BlogTagDto relation : mapper.selectTagsByBlogIds(blogIds)) {
            Tag tag = new Tag();
            tag.setId(relation.getTagId());
            tag.setTagName(relation.getTagName());
            tags.computeIfAbsent(relation.getBlogId(), id -> new ArrayList<>()).add(tag);
        }
        for (Blog blog : blogs) {
            blog.setType(types.get(blog.getTypeId()));
            blog.setUser(users.get(blog.getUserId()));
            blog.setTags(tags.getOrDefault(blog.getId(), new ArrayList<>()));
        }
    }
}
