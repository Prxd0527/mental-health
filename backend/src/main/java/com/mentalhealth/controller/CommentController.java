package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Comment;
import com.mentalhealth.service.CommentService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发布评论（需登录，自动 DFA 过滤）
     */
    @PostMapping("/publish")
    public Result<String> publishComment(@RequestBody Comment comment) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        comment.setUserId(userId);
        if (commentService.publishComment(comment)) {
            return Result.success("评论成功");
        }
        return Result.error("评论失败");
    }

    /**
     * 获取指定树洞的评论列表（支持分页）
     */
    @GetMapping("/list/{postId}")
    public Result<Page<Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<Comment> page = commentService.getCommentsByPostIdPaged(postId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取指定树洞的全部评论（兼容旧接口，不分页）
     */
    @GetMapping("/all/{postId}")
    public Result<List<Comment>> getAllComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    /**
     * 获取指定树洞的评论（树形嵌套结构，支持楼中楼展示）
     */
    @GetMapping("/tree/{postId}")
    public Result<List<Comment>> getCommentsTree(@PathVariable Long postId) {
        List<Comment> tree = commentService.getCommentsTree(postId);
        return Result.success(tree);
    }

    /**
     * 评论点赞
     */
    @PostMapping("/{id}/like")
    public Result<String> likeComment(@PathVariable Long id) {
        if (commentService.likeComment(id)) {
            return Result.success("点赞成功");
        }
        return Result.error("点赞失败");
    }
}
