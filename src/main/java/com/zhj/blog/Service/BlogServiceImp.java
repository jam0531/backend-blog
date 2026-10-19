package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Dto.BlogTagDto;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import com.zhj.blog.Dto.blog.BlogDetailDto;
import com.zhj.blog.Dto.blog.BlogQuery;
import com.zhj.blog.Dto.blog.TagCountDto;
import com.zhj.blog.Dto.blog.AdminBlogDto;
import com.zhj.blog.handler.ApiStatusException;
import org.springframework.http.HttpStatus;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.TypeMapper;
import com.zhj.blog.Mapper.TagMapper;
import com.zhj.blog.Mapper.UserMapper;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Tag;
import com.zhj.blog.Pojo.Type;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.Pojo.Notice;
import com.zhj.blog.Utils.MarkdownUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    private TagMapper tagMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NoticeService noticeService;

    // 只读联调可显式关闭计数；正常运行保留浏览文章时增加阅读量的业务行为。
    @Value("${blog.reading-count-enabled:true}")
    private boolean readingCountEnabled;

    @Override
    public PageResult<AdminBlogDto.Item> getAdminBlogPage(AdminBlogDto.Query query) {
        QueryWrapper<Blog> wrapper = new QueryWrapper<Blog>().select("id", "title", "description", "blog_pic",
                "flag", "views", "create_time", "update_time", "type_id", "user_id", "publish", "recommend");
        if (query.getTypeId() != null) wrapper.eq("type_id", query.getTypeId());
        if (query.getPublish() != null) wrapper.eq("publish", query.getPublish());
        if (query.getRecommend() != null) wrapper.eq("recommend", query.getRecommend());
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
            wrapper.apply("(title LIKE {0} ESCAPE '!' OR description LIKE {0} ESCAPE '!')", "%" + keyword + "%");
        }
        Page<Blog> page = mapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper.orderByDesc("update_time", "id"));
        populateRelations(page.getRecords());
        List<AdminBlogDto.Item> items = page.getRecords().stream().map(blog -> {
            AdminBlogDto.Item item = new AdminBlogDto.Item();
            BeanUtils.copyProperties(BlogSummaryDto.from(blog), item);
            item.setPublish(blog.isPublish());
            item.setRecommend(blog.isRecommend());
            return item;
        }).collect(Collectors.toList());
        return new PageResult<>(items, page.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public AdminBlogDto.Edit getAdminBlogEdit(Long id) {
        Blog blog = mapper.selectById(id);
        if (blog == null) throw new NotFoundException("文章不存在");
        AdminBlogDto.Edit dto = new AdminBlogDto.Edit();
        dto.setId(blog.getId().toString());
        dto.setTitle(blog.getTitle());
        dto.setDescription(blog.getDescription());
        dto.setBlogPic(blog.getBlogPic());
        dto.setFlag(blog.getFlag());
        dto.setContentMarkdown(blog.getContent());
        dto.setTypeId(blog.getTypeId() == null ? null : blog.getTypeId().toString());
        dto.setTagIds(mapper.selectTagIds(id).stream().map(String::valueOf).collect(Collectors.toList()));
        dto.setPublish(blog.isPublish());
        dto.setRecommend(blog.isRecommend());
        dto.setCommentAble(blog.isCommentAble());
        dto.setShareStatement(blog.isShareStatement());
        return dto;
    }

    @Override
    @Transactional
    public AdminBlogDto.Saved saveAdminBlog(Long id, AdminBlogDto.Input input, Long authorId) {
        Blog blog = id == null ? new Blog() : mapper.lockById(id);
        if (blog == null) throw new NotFoundException("文章不存在");
        // 与分类/标签删除使用同一行锁；固定标签加锁顺序，避免并发保存时形成反向锁序。
        if (typeMapper.lockById(input.getTypeId()) == null) {
            throw new ApiStatusException(HttpStatus.BAD_REQUEST, "分类不存在，请重新选择");
        }
        List<Long> tagIds = new ArrayList<>(new TreeSet<>(input.getTagIds()));
        for (Long tagId : tagIds) {
            if (tagMapper.lockById(tagId) == null) {
                throw new ApiStatusException(HttpStatus.BAD_REQUEST, "标签不存在，请重新选择");
            }
        }
        blog.setTitle(input.getTitle().trim());
        blog.setDescription(input.getDescription().trim());
        blog.setContent(input.getContentMarkdown());
        blog.setBlogPic(input.getBlogPic().trim());
        blog.setFlag(input.getFlag());
        blog.setTypeId(input.getTypeId());
        blog.setPublish(input.getPublish());
        blog.setRecommend(input.getRecommend());
        blog.setShareStatement(input.getShareStatement());
        blog.setCommentAble(input.getCommentAble());
        blog.setUpdateTime(LocalDateTime.now());
        if (id == null) {
            blog.setUserId(authorId);
            blog.setViews(0);
            blog.setCreateTime(blog.getUpdateTime());
            mapper.insert(blog); // ASSIGN_ID 回填主键后，再写关联记录。
        } else {
            mapper.updateContent(blog);
        }
        mapper.deleteTagLinks(blog.getId());
        if (!tagIds.isEmpty()) mapper.insertTagLinks(blog.getId(), tagIds);
        return new AdminBlogDto.Saved(blog.getId().toString(), blog.isPublish());
    }

    @Override
    @Transactional
    public void deleteAdminBlog(Long id) {
        if (mapper.lockById(id) == null) throw new NotFoundException("文章不存在");
        mapper.deleteBlogComments(id);
        mapper.deleteTagLinks(id);
        mapper.deleteById(id);
    }

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
        BlogQuery query = new BlogQuery();
        query.setPage(page);
        query.setSize(size);
        return getPublicBlogPage(query);
    }

    @Override
    public PageResult<BlogSummaryDto> getPublicBlogPage(BlogQuery query) {
        int page = query.getPage();
        int size = query.getSize();
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 必须大于等于 1，size 必须在 1 到 100 之间");
        }
        QueryWrapper<Blog> wrapper = publicSummaryQuery();
        if (query.getTypeId() != null) {
            wrapper.eq("type_id", query.getTypeId());
        }
        if (query.getTagId() != null) {
            // EXISTS 避免旧关联表中的重复行造成分页文章重复，不拼接客户端 SQL。
            wrapper.apply("EXISTS (SELECT 1 FROM blog_table_tags bt WHERE bt.blogs_id = blog_table.id " +
                    "AND bt.tags_id = {0})", query.getTagId());
        }
        if (query.getYear() != null) {
            LocalDateTime start = LocalDateTime.of(query.getYear(), 1, 1, 0, 0);
            wrapper.ge("create_time", start);
            if (query.getYear() < 9999) {
                wrapper.lt("create_time", start.plusYears(1));
            }
        }
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            // 搜索范围已确认：标题和摘要；将 %、_ 按普通字符匹配。
            String keyword = query.getKeyword().trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
            wrapper.apply("(title LIKE {0} ESCAPE '!' OR description LIKE {0} ESCAPE '!')", "%" + keyword + "%");
        }
        Page<Blog> result = mapper.selectPage(new Page<>(page, size), wrapper.orderByDesc("update_time", "id"));
        return new PageResult<>(toSummaries(result.getRecords()), result.getTotal(), page, size);
    }

    @Override
    public List<HomeDto.TypeCountDto> getPublicTypes() {
        return typeMapper.selectPublishedTypes();
    }

    @Override
    public List<TagCountDto> getPublicTags() {
        return tagMapper.selectPublishedTags();
    }

    @Override
    public List<HomeDto.ArchiveDto> getPublicArchives() {
        return mapper.selectPublishedArchives();
    }

    @Override
    public HomeDto getPublicHome() {
        HomeDto dto = new HomeDto();
        Notice notice = noticeService.getLatestNotice();
        if (notice != null) {
            dto.setNotice(new HomeDto.NoticeDto(notice.getId().toString(),
                    notice.getContent(), notice.getCreateTime()));
        }
        List<Blog> recommended = mapper.selectList(publicSummaryQuery().eq("recommend", 1)
                .orderByDesc("create_time", "id").last("LIMIT 6"));
        dto.setRecommendedBlogs(toSummaries(recommended));
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
    @Transactional
    public Blog getBlogByIdAndContentConvert(Long id) {
        Blog blog = mapper.selectOne(new QueryWrapper<Blog>().eq("id", id).eq("publish", 1));
        if (blog == null){
            throw new NotFoundException("该博客不存在");
        }
        populateRelations(Collections.singletonList(blog));
        // 新建一个Blog对象进行内容转换避免修改数据库中的内容
        Blog blog1 = new Blog();
        BeanUtils.copyProperties(blog, blog1);
        String content = blog1.getContent();
        blog1.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
        // 旧入口与 API 共用计数开关和原子 SQL，避免读取旧对象后覆盖后台编辑结果。
        if (readingCountEnabled) {
            if (mapper.incrementPublishedViews(id) == 0) {
                throw new NotFoundException("该博客不存在");
            }
            blog1.setViews(mapper.selectPublishedViews(id));
        }
        return blog1;
    }

    @Override
    public Map<String, List<Blog>> archiveBlog() {
        List<Blog> blogs = mapper.selectList(new QueryWrapper<Blog>().eq("publish", 1).orderByDesc("create_time"));
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
