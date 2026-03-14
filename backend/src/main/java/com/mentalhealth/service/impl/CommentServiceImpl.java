package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Comment;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.CommentMapper;
import com.mentalhealth.service.CommentService;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.SensitiveWordUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private SensitiveWordUtils sensitiveWordUtils;

    @Resource
    private UserService userService;

    @Override
    public boolean publishComment(Comment comment) {
        if (StrUtil.isBlank(comment.getContent())) {
            throw new RuntimeException("评论内容不能为空");
        }

        // DFA脱敏
        if (sensitiveWordUtils.containsSensitiveWord(comment.getContent())) {
            String safeContent = sensitiveWordUtils.replaceSensitiveWord(comment.getContent());
            comment.setContent(safeContent);
        }

        // 初始属性
        comment.setLikes(0);
        comment.setStatus(1);

        // 缺少匿名判断则默认匿名
        if (comment.getIsAnonymous() == null) {
            comment.setIsAnonymous(1);
        }

        return this.save(comment);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime); // 先发布的在上面

        List<Comment> list = baseMapper.selectList(wrapper);
        for (Comment comment : list) {
            fillAuthorInfo(comment);
        }
        return list;
    }

    @Override
    public Page<Comment> getCommentsByPostIdPaged(Long postId, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime);

        Page<Comment> resultPage = baseMapper.selectPage(page, wrapper);
        for (Comment comment : resultPage.getRecords()) {
            fillAuthorInfo(comment);
        }
        return resultPage;
    }

    @Override
    public boolean likeComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null && comment.getStatus() == 1) {
            comment.setLikes(comment.getLikes() + 1);
            return this.updateById(comment);
        }
        throw new RuntimeException("评论不存在或已删除");
    }

    private void fillAuthorInfo(Comment comment) {
        if (comment.getIsAnonymous() != null && comment.getIsAnonymous() == 1) {
            comment.setAuthorName("匿名用户");
            comment.setAuthorAvatar("default_anonymous_avatar.png");
        } else {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                // 回退脱敏机制
                String showName = StrUtil.isNotBlank(user.getRealName()) ? user.getRealName()
                        : StrUtil.hide(user.getUsername(), 1, user.getUsername().length() - 1);
                comment.setAuthorName(showName);
                comment.setAuthorAvatar(user.getAvatar());
            }
        }
    }
}
