package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.entity.Feedback;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.FeedbackMapper;
import com.mentalhealth.service.AppointmentService;
import com.mentalhealth.service.FeedbackService;
import com.mentalhealth.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private UserService userService;

    @Override
    public boolean submitFeedback(Feedback feedback) {
        // 校验评分范围
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("评分范围必须在 1-5 星之间");
        }

        // 校验预约是否存在
        Appointment appointment = appointmentService.getById(feedback.getAppointmentId());
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 仅已完成的预约可以评价
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new RuntimeException("仅已完成的咨询可以评价");
        }

        // 校验是否已评价
        if (hasEvaluated(feedback.getAppointmentId())) {
            throw new RuntimeException("该预约已评价，不可重复提交");
        }

        // 校验当前用户是否是该预约的学生
        if (!appointment.getStudentId().equals(feedback.getStudentId())) {
            throw new RuntimeException("无权评价他人的咨询");
        }

        // 自动填充教师ID
        feedback.setTeacherId(appointment.getTeacherId());

        // 默认匿名
        if (feedback.getIsAnonymous() == null) {
            feedback.setIsAnonymous(1);
        }

        return this.save(feedback);
    }

    @Override
    public List<Feedback> getTeacherFeedbacks(Long teacherId) {
        List<Feedback> list = baseMapper.selectList(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getTeacherId, teacherId)
                .orderByDesc(Feedback::getCreateTime));

        // 补充显示名（非匿名时）
        for (Feedback fb : list) {
            if (fb.getIsAnonymous() != null && fb.getIsAnonymous() == 0) {
                User student = userService.getById(fb.getStudentId());
                if (student != null) {
                    fb.setStudentName(
                            StrUtil.isNotBlank(student.getRealName()) ? student.getRealName() : student.getUsername());
                }
            } else {
                fb.setStudentName("匿名用户");
            }
        }
        return list;
    }

    @Override
    public boolean hasEvaluated(Long appointmentId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getAppointmentId, appointmentId));
        return count > 0;
    }
}
