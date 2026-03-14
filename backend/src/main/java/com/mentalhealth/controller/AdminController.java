package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.*;
import com.mentalhealth.service.*;
import com.mentalhealth.utils.SecurityUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 管理后台控制器
 * 所有接口均需 ROLE_ADMIN 权限（通过 checkAdmin() 校验）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private ArticleService articleService;
    @Resource
    private QuizService quizService;
    @Resource
    private PostService postService;
    @Resource
    private CommentService commentService;

    /**
     * 权限校验：非管理员直接拒绝
     */
    private Result<String> checkAdmin() {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权限执行此操作");
        }
        return null;
    }

    // ======================== 用户管理 ========================

    /**
     * 用户列表（分页 + 角色筛选 + 关键词搜索）
     */
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {

        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(page, wrapper);
        // 脱敏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    /**
     * 封禁/解禁用户
     */
    @PostMapping("/user/{id}/status/{status}")
    public Result<String> updateUserStatus(@PathVariable Long id, @PathVariable Integer status) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        User user = userService.getById(id);
        if (user == null)
            return Result.error("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        return Result.success("用户状态更新成功");
    }

    /**
     * 重置用户密码（管理员操作，重置为默认密码 123456）
     */
    @PostMapping("/user/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        User user = userService.getById(id);
        if (user == null)
            return Result.error("用户不存在");
        // 使用 userService 的 changePassword 不合适（需要旧密码），直接操作
        org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        user.setPassword(encoder.encode("123456"));
        userService.updateById(user);
        return Result.success("密码已重置为 123456");
    }

    // ======================== 咨询师信息维护 ========================

    /**
     * 更新咨询师信息（简介、擅长领域等）
     */
    @PutMapping("/teacher/{id}")
    public Result<String> updateTeacherInfo(@PathVariable Long id, @RequestBody User teacherData) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        User teacher = userService.getById(id);
        if (teacher == null)
            return Result.error("用户不存在");
        if (!"TEACHER".equals(teacher.getRole()))
            return Result.error("该用户不是咨询师");

        if (StrUtil.isNotBlank(teacherData.getRealName()))
            teacher.setRealName(teacherData.getRealName());
        if (StrUtil.isNotBlank(teacherData.getIntro()))
            teacher.setIntro(teacherData.getIntro());
        if (StrUtil.isNotBlank(teacherData.getExpertise()))
            teacher.setExpertise(teacherData.getExpertise());
        if (StrUtil.isNotBlank(teacherData.getAvatar()))
            teacher.setAvatar(teacherData.getAvatar());
        if (teacherData.getGender() != null)
            teacher.setGender(teacherData.getGender());

        userService.updateById(teacher);
        return Result.success("咨询师信息更新成功");
    }

    // ======================== 文章管理 ========================

    /**
     * 文章列表（管理端，包含全部状态，分页 + 搜索）
     */
    @GetMapping("/article/list")
    public Result<Page<Article>> getArticleList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        return Result.success(articleService.page(page, wrapper));
    }

    /**
     * 保存/更新文章
     */
    @PostMapping("/article/save")
    public Result<String> saveOrUpdateArticle(@RequestBody Article article) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        articleService.saveOrUpdate(article);
        return Result.success("文章保存成功");
    }

    /**
     * 文章上下架
     */
    @PostMapping("/article/{id}/status/{status}")
    public Result<String> updateArticleStatus(@PathVariable Long id, @PathVariable Integer status) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        Article article = articleService.getById(id);
        if (article == null)
            return Result.error("文章不存在");
        article.setStatus(status);
        articleService.updateById(article);
        return Result.success("文章状态更新成功");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/article/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        articleService.removeById(id);
        return Result.success("文章已删除");
    }

    // ======================== 问卷管理 ========================

    /**
     * 问卷列表（管理端，含全部状态）
     */
    @GetMapping("/quiz/list")
    public Result<Page<Quiz>> getQuizList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {

        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Page<Quiz> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Quiz::getStatus, status);
        }
        wrapper.orderByDesc(Quiz::getCreateTime);
        return Result.success(quizService.page(page, wrapper));
    }

    /**
     * 保存/更新问卷
     */
    @PostMapping("/quiz/save")
    public Result<String> saveOrUpdateQuiz(@RequestBody Quiz quiz) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        quizService.saveOrUpdate(quiz);
        return Result.success("问卷保存成功");
    }

    /**
     * 问卷上下线
     */
    @PostMapping("/quiz/{id}/status/{status}")
    public Result<String> updateQuizStatus(@PathVariable Long id, @PathVariable Integer status) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        Quiz quiz = quizService.getById(id);
        if (quiz == null)
            return Result.error("问卷不存在");
        quiz.setStatus(status);
        quizService.updateById(quiz);
        return Result.success("问卷状态更新成功");
    }

    // ======================== 题目 CRUD ========================

    /**
     * 获取指定问卷的题目列表
     */
    @GetMapping("/quiz/{quizId}/questions")
    public Result<List<Question>> getQuestions(@PathVariable Long quizId) {
        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");
        return Result.success(quizService.getQuestions(quizId));
    }

    /**
     * 新增/更新题目
     */
    @PostMapping("/question/save")
    public Result<String> saveOrUpdateQuestion(@RequestBody Question question) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        quizService.saveOrUpdateQuestion(question);
        return Result.success("题目保存成功");
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/question/{id}")
    public Result<String> deleteQuestion(@PathVariable Long id) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        quizService.removeQuestionById(id);
        return Result.success("题目已删除");
    }

    // ======================== 内容审核（树洞/评论） ========================

    /**
     * 树洞审核列表（全部状态，分页）
     */
    @GetMapping("/post/list")
    public Result<Page<Post>> getPostList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {

        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        return Result.success(postService.page(page, wrapper));
    }

    /**
     * 管理端强制下架/恢复树洞
     */
    @PostMapping("/post/{id}/status/{status}")
    public Result<String> updatePostStatus(@PathVariable Long id, @PathVariable Integer status) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;

        Post post = postService.getById(id);
        if (post == null)
            return Result.error("树洞不存在");
        post.setStatus(status);
        postService.updateById(post);
        return Result.success("树洞状态更新成功");
    }

    /**
     * 评论审核列表（全部评论，分页）
     */
    @GetMapping("/comment/list")
    public Result<Page<Comment>> getCommentList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long postId) {

        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (postId != null) {
            wrapper.eq(Comment::getPostId, postId);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        return Result.success(commentService.page(page, wrapper));
    }

    /**
     * 管理端删除违规评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        Result<String> check = checkAdmin();
        if (check != null)
            return check;
        commentService.removeById(id);
        return Result.success("评论已删除");
    }

    // ======================== 数据统计（仪表盘概览） ========================

    /**
     * 获取系统概况统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Result<String> check = checkAdmin();
        if (check != null)
            return Result.error(403, "无权限执行此操作");

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("totalStudents", userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")));
        stats.put("totalTeachers", userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER")));
        stats.put("totalPosts", postService.count());
        stats.put("totalArticles", articleService.count());
        stats.put("totalQuizzes", quizService.count());
        stats.put("totalComments", commentService.count());
        return Result.success(stats);
    }
}
