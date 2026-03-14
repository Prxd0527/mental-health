package com.mentalhealth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Post;
import com.mentalhealth.service.PostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping("/square")
    public Result<Page<Post>> getPostSquare(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(required = false) String tags) {

        Page<Post> page = postService.getPostSquare(pageNum, pageSize, sortBy, tags);
        return Result.success(page);
    }

    @PostMapping("/publish")
    public Result<String> publish(@RequestBody Post post) {
        // 从Spring Security上下文获取当前操作的用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "尚未登录");
        }
        // 注意：在 JwtAuthenticationFilter 我们将 userId 放到了 Principal 字段里
        Long userId = (Long) auth.getPrincipal();
        post.setUserId(userId);

        if (postService.publishPost(post)) {
            return Result.success("发布成功");
        }
        return Result.error("发布失败");
    }

    @GetMapping("/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        Post post = postService.getPostDetail(id);
        return Result.success(post);
    }

    @PostMapping("/{id}/like")
    public Result<String> likePost(@PathVariable Long id) {
        if (postService.likePost(id)) {
            return Result.success("点赞成功");
        }
        return Result.error("点赞失败");
    }
}
