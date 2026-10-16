package com.zhj.blog.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author:zouhuajun
 * @date:2022/7/26 14:48
 **/
@Controller
public class IndexController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TagService tagService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private PictureService pictureService;

    // 首页内容
    @GetMapping("/")
    public String Index(Model model){
        model.addAttribute("bg",pictureService.getRandomPic(1));
        return "index";
    }

    // 主页内容
    @GetMapping("/home")
    public String home(Page page, Model model){
        model.addAttribute("page",blogService.PublishedBlogList(page));
        model.addAttribute("TopBlogs", blogService.BlogTopList(5));
        model.addAttribute("notice", noticeService.getLatestNotice());
        model.addAttribute("topTypes",typeService.TypeTopList(5));
        model.addAttribute("recommendBlogs",blogService.RecommendBlogList(6));
        model.addAttribute("archives", blogService.archiveBlog());
        getTypeAndTagAndArticleNum(model);
        return "home";
    }

    @PostMapping("/publishBlogs")
    public String blogs(Page page, Model model){
        model.addAttribute("page",blogService.PublishedBlogList(page));
        return "home :: blogList";
    }

    // 单个博客内容
    @GetMapping("/blog/{id}")
    public String blog(@PathVariable Long id, Model model){
        getTypeAndTagAndArticleNum(model);
        model.addAttribute("blog", blogService.getBlogByIdAndContentConvert(id));
//        model.addAttribute("commentArray", commentService.listAllCommentsByBlogId(id));
        return "blog";
    }

    // 获取所有的标签和分类以及发布的博客
    private void getTypeAndTagAndArticleNum(Model model) {
        model.addAttribute("publishedBlogs", blogService.list(new QueryWrapper<Blog>().eq("publish",1).orderByDesc("update_time")));
        model.addAttribute("totalTypes", typeService.list());
        model.addAttribute("totalTags", tagService.list(null));
    }
}
