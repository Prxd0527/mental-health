package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Post;
import com.mentalhealth.service.PostService;
import com.mentalhealth.utils.SecurityUtils;
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
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

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

    /**
     * 我的树洞 - 查看当前用户发布的所有树洞（含已下架）
     */
    @GetMapping("/my")
    public Result<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);

        Page<Post> resultPage = postService.page(page, wrapper);
        return Result.success(resultPage);
    }

    /**
     * 删除自己的树洞（逻辑删除，将状态置为0）
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("树洞不存在");
        }
        // 只允许本人或管理员删除
        if (!post.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            return Result.error(403, "无权删除他人的树洞");
        }
        post.setStatus(0); // 逻辑删除
        postService.updateById(post);
        return Result.success("删除成功");
    }
}
