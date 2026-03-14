package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Comment;
import com.mentalhealth.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/publish")
    public Result<String> publishComment(@RequestBody Comment comment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "尚未登录");
        }
        Long userId = (Long) auth.getPrincipal();
        comment.setUserId(userId);

        if (commentService.publishComment(comment)) {
            return Result.success("评论成功");
        }
        return Result.error("评论失败");
    }

    @GetMapping("/list/{postId}")
    public Result<List<Comment>> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    @PostMapping("/{id}/like")
    public Result<String> likeComment(@PathVariable Long id) {
        if (commentService.likeComment(id)) {
            return Result.success("点赞成功");
        }
        return Result.error("点赞失败");
    }
}
