package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Post;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.PostMapper;
import com.mentalhealth.service.PostService;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.SensitiveWordUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private SensitiveWordUtils sensitiveWordUtils;

    @Resource
    private UserService userService;

    @Override
    public boolean publishPost(Post post) {
        if (StrUtil.isBlank(post.getContent())) {
            throw new RuntimeException("树洞内容不能为空");
        }

        // 关键业务逻辑：DFA敏感词脱敏替换
        if (sensitiveWordUtils.containsSensitiveWord(post.getContent())) {
            String safeContent = sensitiveWordUtils.replaceSensitiveWord(post.getContent());
            post.setContent(safeContent);
        }

        // 初始化基础属性
        post.setLikes(0);
        post.setViews(0);
        post.setStatus(1);

        // 若前端没有明确传递匿名状态，默认匿名
        if (post.getIsAnonymous() == null) {
            post.setIsAnonymous(1);
        }

        return this.save(post);
    }

    @Override
    public Page<Post> getPostSquare(int pageNum, int pageSize, String sortBy, String tags) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        // 标签过滤
        if (StrUtil.isNotBlank(tags)) {
            wrapper.like(Post::getTags, tags);
        }

        // 动态排序
        if ("hot".equalsIgnoreCase(sortBy)) {
            // 热度：优先按点赞数降序，其次浏览量，最后时间
            wrapper.orderByDesc(Post::getLikes, Post::getViews, Post::getCreateTime);
        } else {
            // 默认 latest：按发布时间倒序
            wrapper.orderByDesc(Post::getCreateTime);
        }

        Page<Post> resultPage = baseMapper.selectPage(page, wrapper);

        // 对返回结果进行用户信息脱敏赋予
        List<Post> records = resultPage.getRecords();
        for (Post post : records) {
            fillAuthorInfo(post);
        }

        return resultPage;
    }

    @Override
    public boolean likePost(Long postId) {
        Post post = this.getById(postId);
        if (post != null && post.getStatus() == 1) {
            post.setLikes(post.getLikes() + 1);
            return this.updateById(post);
        }
        throw new RuntimeException("操作失败，该内容已失效");
    }

    @Override
    public Post getPostDetail(Long postId) {
        Post post = this.getById(postId);
        if (post != null && post.getStatus() == 1) {
            post.setViews(post.getViews() + 1);
            this.updateById(post);
            fillAuthorInfo(post);
            return post;
        }
        throw new RuntimeException("树洞不存在或已封禁");
    }

    /**
     * 封装作者信息（处理匿名逻辑）
     */
    private void fillAuthorInfo(Post post) {
        if (post.getIsAnonymous() != null && post.getIsAnonymous() == 1) {
            post.setAuthorName("匿名用户");
            post.setAuthorAvatar("default_anonymous_avatar.png");
        } else {
            User user = userService.getById(post.getUserId());
            if (user != null) {
                // 若无真实姓名，回退显示脱敏的学号/邮箱
                String showName = StrUtil.isNotBlank(user.getRealName()) ? user.getRealName()
                        : StrUtil.hide(user.getUsername(), 1, user.getUsername().length() - 1);
                post.setAuthorName(showName);
                post.setAuthorAvatar(user.getAvatar());
            }
        }
    }
}
