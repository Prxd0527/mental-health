package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Article;
import com.mentalhealth.entity.Quiz;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.ArticleService;
import com.mentalhealth.service.QuizService;
import com.mentalhealth.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Resource
    private QuizService quizService;

    /**
     * 校验后台操作者是否具备管理权限（此处作简化，依靠 SecurityContext 的角色区分更佳）
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // --- 用户治理 ---

    @PostMapping("/user/{id}/status/{status}")
    public Result<String> updateUserStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (!isAdmin())
            return Result.error(403, "无权限执行此操作");

        User user = userService.getById(id);
        if (user != null) {
            user.setStatus(status); // 0-封禁, 1-正常
            userService.updateById(user);
            return Result.success("用户状态更新成功");
        }
        return Result.error("找不着此用户");
    }

    // --- 科普文章运维 ---

    @PostMapping("/article/save")
    public Result<String> saveOrUpdateArticle(@RequestBody Article article) {
        if (!isAdmin())
            return Result.error(403, "无权限执行此操作");

        articleService.saveOrUpdate(article);
        return Result.success("文章保存成功");
    }

    @PostMapping("/article/{id}/status/{status}")
    public Result<String> updateArticleStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (!isAdmin())
            return Result.error(403, "无权限执行此操作");

        Article article = articleService.getById(id);
        if (article != null) {
            article.setStatus(status); // 0-下架/草稿, 1-发布
            articleService.updateById(article);
            return Result.success("文章状态更新成功");
        }
        return Result.error("文章不存在");
    }

    // --- 问卷测评运维 ---

    @PostMapping("/quiz/save")
    public Result<String> saveOrUpdateQuiz(@RequestBody Quiz quiz) {
        if (!isAdmin())
            return Result.error(403, "无权限执行此操作");

        quizService.saveOrUpdate(quiz);
        return Result.success("问卷保存成功");
    }

    @PostMapping("/quiz/{id}/status/{status}")
    public Result<String> updateQuizStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (!isAdmin())
            return Result.error(403, "无权限执行此操作");

        Quiz quiz = quizService.getById(id);
        if (quiz != null) {
            quiz.setStatus(status); // 0-下架, 1-上线
            quizService.updateById(quiz);
            return Result.success("问卷状态更新成功");
        }
        return Result.error("问卷不存在");
    }
}
