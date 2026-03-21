package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Feedback;
import com.mentalhealth.service.FeedbackService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 学生提交评价
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Feedback feedback) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        feedback.setStudentId(userId);
        if (feedbackService.submitFeedback(feedback)) {
            return Result.success("评价提交成功，感谢您的反馈！");
        }
        return Result.error("评价提交失败");
    }

    /**
     * 获取某位教师的所有评价（公开）
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Feedback>> getTeacherFeedbacks(@PathVariable Long teacherId) {
        List<Feedback> list = feedbackService.getTeacherFeedbacks(teacherId);
        return Result.success(list);
    }

    /**
     * 检查某个预约是否已评价
     */
    @GetMapping("/check/{appointmentId}")
    public Result<Boolean> checkEvaluated(@PathVariable Long appointmentId) {
        return Result.success(feedbackService.hasEvaluated(appointmentId));
    }
}
