package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Dto.PageQuery;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.discussion.DiscussionDto.*;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.CommentMapper;
import com.zhj.blog.Mapper.MessageMapper;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.Pojo.*;
import com.zhj.blog.handler.ApiStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final BlogMapper blogs;
    private final CommentMapper comments;
    private final MessageMapper messages;

    @Transactional(readOnly = true)
    public PageResult<Item> comments(Long blogId, PageQuery query) {
        Blog blog = blogs.selectById(blogId);
        if (blog == null || !blog.isPublish()) throw new NotFoundException("文章不存在");
        Page<Comment> page = comments.selectPage(new Page<>(query.getPage(), query.getSize()),
                new QueryWrapper<Comment>().eq("blog_id", blogId)
                        .and(w -> w.isNull("parent_comment_id").or().le("parent_comment_id", 0))
                        .orderByDesc("create_time", "id"));
        List<Item> roots = page.getRecords().stream().map(this::item).collect(Collectors.toList());
        attachReplies(roots, ids -> comments.selectList(new QueryWrapper<Comment>().eq("blog_id", blogId)
                .in("parent_comment_id", ids)).stream().map(this::item).collect(Collectors.toList()));
        return new PageResult<>(roots, page.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<Item> messages(MessageQuery query) {
        Page<Message> page = messages.selectPage(new Page<>(query.getPage(), query.getSize()),
                new QueryWrapper<Message>().eq("message_type", query.getMessageType())
                        .and(w -> w.isNull("parent_message_id").or().le("parent_message_id", 0))
                        .orderByDesc("create_time", "id"));
        List<Item> roots = page.getRecords().stream().map(this::item).collect(Collectors.toList());
        attachReplies(roots, ids -> messages.selectList(new QueryWrapper<Message>().eq("message_type", query.getMessageType())
                .in("parent_message_id", ids)).stream().map(this::item).collect(Collectors.toList()));
        return new PageResult<>(roots, page.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<AdminItem> adminComments(AdminQuery query) {
        Page<Comment> page = comments.selectPage(new Page<>(query.getPage(), query.getSize()),
                new QueryWrapper<Comment>().eq(query.getBlogId() != null, "blog_id", query.getBlogId()).orderByDesc("create_time", "id"));
        Set<Long> blogIds = page.getRecords().stream().map(Comment::getBlogId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> titles = new HashMap<>();
        if (!blogIds.isEmpty()) blogs.selectBatchIds(blogIds).forEach(blog -> titles.put(blog.getId(), blog.getTitle()));
        List<AdminItem> items = new ArrayList<>();
        for (Comment comment : page.getRecords()) {
            AdminItem item = new AdminItem(); fill(item, comment); item.setEmail(comment.getEmail());
            item.setBlogTitle(titles.get(comment.getBlogId())); items.add(item);
        }
        return new PageResult<>(items, page.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<AdminItem> adminMessages(AdminQuery query) {
        Page<Message> page = messages.selectPage(new Page<>(query.getPage(), query.getSize()),
                new QueryWrapper<Message>().eq(query.getMessageType() != null, "message_type", query.getMessageType()).orderByDesc("create_time", "id"));
        List<AdminItem> items = new ArrayList<>();
        for (Message message : page.getRecords()) {
            AdminItem item = new AdminItem(); fill(item, message); item.setEmail(message.getEmail()); items.add(item);
        }
        return new PageResult<>(items, page.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Item addComment(Long blogId, Input input, User user) {
        // 使用 READ_COMMITTED，让等待行锁后执行的父级查询读取最新已提交数据。
        Blog blog = blogs.lockById(blogId);
        if (blog == null || !blog.isPublish()) throw new NotFoundException("文章不存在");
        if (!blog.isCommentAble()) throw new ApiStatusException(HttpStatus.CONFLICT, "这篇文章已关闭评论");
        return saveComment(blogId, input.getParentId(), input.getContent(), input.getNickname(), input.getEmail(), user);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Item replyComment(Long parentId, ReplyInput input, User admin) {
        Comment parent = comments.selectById(parentId);
        if (parent == null || parent.getBlogId() == null || blogs.lockById(parent.getBlogId()) == null) {
            throw new NotFoundException("评论或文章不存在");
        }
        // 后台管理员可回复已关闭评论的文章；公开提交接口仍受 commentAble 限制。
        return saveComment(parent.getBlogId(), parentId, input.getContent(), null, null, admin);
    }

    private Item saveComment(Long blogId, Long parentId, String content, String nickname, String email, User user) {
        Comment parent = parentId == null ? null : comments.selectById(parentId);
        if (parentId != null && (parent == null || !blogId.equals(parent.getBlogId()))) {
            throw new ApiStatusException(HttpStatus.BAD_REQUEST, "回复对象不存在或不属于该文章");
        }
        Comment comment = new Comment(); comment.setBlogId(blogId); comment.setParentCommentId(parentId);
        comment.setContent(content.trim()); boolean admin = isAdmin(user); comment.setAdminComment(admin);
        comment.setNickname(admin ? adminName(user) : nickname.trim());
        comment.setEmail(admin ? user.getEmail() : email.trim()); comment.setAvatar(admin ? user.getAvatar() : null);
        comments.insert(comment); Item result = item(comment); result.setReplyTo(parent == null ? null : parent.getNickname()); return result;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Item addMessage(MessageInput input, User user) {
        return saveMessage(input.getMessageType(), input.getParentId(), input.getContent(), input.getNickname(), input.getEmail(), user);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Item replyMessage(Long parentId, ReplyInput input, User admin) {
        Message parent = messages.selectById(parentId);
        if (parent == null) throw new NotFoundException("留言不存在");
        return saveMessage(parent.getMessageType(), parentId, input.getContent(), null, null, admin);
    }

    private Item saveMessage(Integer type, Long parentId, String content, String nickname, String email, User user) {
        Message parent = null;
        if (parentId != null) {
            Long rootId = messageRoot(parentId, type);
            if (messages.lockById(rootId) == null) throw new NotFoundException("留言已被删除");
            // 再次查询父级，覆盖等待根锁期间父回复被删除的情况。
            parent = messages.selectById(parentId);
            if (parent == null || !type.equals(parent.getMessageType())) throw new NotFoundException("回复对象不存在");
        }
        Message message = new Message(); message.setMessageType(type); message.setParentMessageId(parentId);
        message.setContent(content.trim()); boolean admin = isAdmin(user); message.setAdminMessage(admin);
        message.setNickname(admin ? adminName(user) : nickname.trim());
        message.setEmail(admin ? user.getEmail() : email.trim()); message.setAvatar(admin ? user.getAvatar() : null);
        messages.insert(message); Item result = item(message); result.setReplyTo(parent == null ? null : parent.getNickname()); return result;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteComment(Long id) {
        Comment target = comments.selectById(id);
        if (target == null) throw new NotFoundException("评论不存在");
        if (target.getBlogId() != null) blogs.lockById(target.getBlogId());
        List<Long> ids = descendants(id, parents -> comments.selectList(new QueryWrapper<Comment>()
                .eq(target.getBlogId() != null, "blog_id", target.getBlogId()).isNull(target.getBlogId() == null, "blog_id")
                .in("parent_comment_id", parents))
                .stream().map(Comment::getId).collect(Collectors.toList()));
        deleteBatches(ids, batch -> comments.deleteBatchIds(batch));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteMessage(Long id) {
        Message target = messages.selectById(id);
        if (target == null) throw new NotFoundException("留言不存在");
        if (messages.lockById(messageRoot(id, target.getMessageType())) == null) throw new NotFoundException("留言不存在");
        List<Long> ids = descendants(id, parents -> messages.selectList(new QueryWrapper<Message>()
                .eq("message_type", target.getMessageType()).in("parent_message_id", parents))
                .stream().map(Message::getId).collect(Collectors.toList()));
        deleteBatches(ids, batch -> messages.deleteBatchIds(batch));
    }

    private Long messageRoot(Long id, Integer type) {
        Set<Long> seen = new HashSet<>(); Long current = id;
        while (current != null && current > 0 && seen.add(current)) {
            Message message = messages.selectById(current);
            if (message == null || !Objects.equals(type, message.getMessageType())) {
                throw new ApiStatusException(HttpStatus.BAD_REQUEST, "回复对象不存在或留言类型不一致");
            }
            if (message.getParentMessageId() == null || message.getParentMessageId() <= 0) return current;
            current = message.getParentMessageId();
        }
        throw new ApiStatusException(HttpStatus.CONFLICT, "留言关联异常，暂时无法操作");
    }

    /** 分层批量读取，避免递归栈溢出及逐条 N+1；visited 阻断异常循环关系。 */
    private void attachReplies(List<Item> roots, Function<List<Long>, List<Item>> fetch) {
        Map<String, Item> owners = new HashMap<>(), known = new HashMap<>();
        List<Long> frontier = new ArrayList<>();
        for (Item root : roots) { owners.put(root.getId(), root); known.put(root.getId(), root); frontier.add(Long.valueOf(root.getId())); }
        while (!frontier.isEmpty()) {
            List<Long> next = new ArrayList<>();
            for (int offset = 0; offset < frontier.size(); offset += 200) {
                for (Item reply : fetch.apply(frontier.subList(offset, Math.min(offset + 200, frontier.size())))) {
                    Item owner = owners.get(reply.getParentId());
                    if (owner == null || known.containsKey(reply.getId())) continue;
                    reply.setReplyTo(known.get(reply.getParentId()).getNickname()); owner.getReplies().add(reply);
                    owners.put(reply.getId(), owner); known.put(reply.getId(), reply); next.add(Long.valueOf(reply.getId()));
                }
            }
            frontier = next;
        }
        Comparator<Item> order = Comparator.comparing(Item::getCreateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(item -> Long.valueOf(item.getId()));
        roots.forEach(root -> root.getReplies().sort(order));
    }

    private List<Long> descendants(Long id, Function<List<Long>, List<Long>> fetch) {
        Set<Long> seen = new LinkedHashSet<>(); seen.add(id); List<Long> frontier = Collections.singletonList(id);
        while (!frontier.isEmpty()) {
            List<Long> next = new ArrayList<>();
            for (int offset = 0; offset < frontier.size(); offset += 200) {
                for (Long child : fetch.apply(frontier.subList(offset, Math.min(offset + 200, frontier.size())))) {
                    if (seen.add(child)) next.add(child);
                }
            }
            frontier = next;
        }
        return new ArrayList<>(seen);
    }

    private void deleteBatches(List<Long> ids, Function<List<Long>, Integer> delete) {
        for (int i = 0; i < ids.size(); i += 200) delete.apply(ids.subList(i, Math.min(i + 200, ids.size())));
    }
    private boolean isAdmin(User user) { return user != null && Integer.valueOf(1).equals(user.getType()); }
    private String adminName(User user) { return user.getAliasname() == null || user.getAliasname().trim().isEmpty() ? "博主" : user.getAliasname(); }
    private String id(Long value) { return value == null || value <= 0 ? null : String.valueOf(value); }
    private Item item(Comment source) { Item item = new Item(); fill(item, source); return item; }
    private Item item(Message source) { Item item = new Item(); fill(item, source); return item; }
    private void fill(Item target, Comment source) {
        target.setId(id(source.getId())); target.setBlogId(id(source.getBlogId())); target.setParentId(id(source.getParentCommentId()));
        target.setNickname(source.getNickname()); target.setContent(source.getContent()); target.setAvatar(source.getAvatar());
        target.setAdmin(source.isAdminComment()); target.setCreateTime(source.getCreateTime());
    }
    private void fill(Item target, Message source) {
        target.setId(id(source.getId())); target.setMessageType(source.getMessageType()); target.setParentId(id(source.getParentMessageId()));
        target.setNickname(source.getNickname()); target.setContent(source.getContent()); target.setAvatar(source.getAvatar());
        target.setAdmin(source.isAdminMessage()); target.setCreateTime(source.getCreateTime());
    }
}
