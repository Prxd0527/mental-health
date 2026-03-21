package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Feedback;

import java.util.List;

public interface FeedbackService extends IService<Feedback> {

    /**
     * 学生提交咨询评价
     */
    boolean submitFeedback(Feedback feedback);

    /**
     * 查询某位教师收到的所有评价
     */
    List<Feedback> getTeacherFeedbacks(Long teacherId);

    /**
     * 检查某个预约是否已经评价过
     */
    boolean hasEvaluated(Long appointmentId);
}
