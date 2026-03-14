package com.mentalhealth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Question;
import com.mentalhealth.entity.Quiz;
import com.mentalhealth.entity.QuizResult;
import com.mentalhealth.service.QuizService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Resource
    private QuizService quizService;

    @GetMapping("/list")
    public Result<Page<Quiz>> getQuizList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<Quiz> page = quizService.getOnlineQuizPage(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}/questions")
    public Result<List<Question>> getQuizQuestions(@PathVariable Long id) {
        List<Question> questions = quizService.getQuestionsByQuizId(id);
        return Result.success(questions);
    }

    @PostMapping("/{id}/submit")
    public Result<QuizResult> submitQuiz(
            @PathVariable Long id,
            @RequestBody Map<Long, Integer> answers) {

        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        QuizResult result = quizService.submitQuiz(id, userId, answers);
        return Result.success(result);
    }

    @GetMapping("/my-results")
    public Result<List<QuizResult>> getMyResults() {
        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<QuizResult> results = quizService.getUserQuizHistory(userId);
        return Result.success(results);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
