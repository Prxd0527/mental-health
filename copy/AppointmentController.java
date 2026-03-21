package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.service.AppointmentService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    // --- 学生端接口 ---

    /**
     * 学生提交预约申请
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Appointment appointment) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        appointment.setStudentId(userId);
        if (appointmentService.submitAppointment(appointment)) {
            return Result.success("预约申请已提交");
        }
        return Result.error("预约失败");
    }

    /**
     * 学生查看我的预约记录
     */
    @GetMapping("/my")
    public Result<List<Appointment>> getMyAppointments() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<Appointment> list = appointmentService.getStudentAppointments(userId);
        return Result.success(list);
    }

    /**
     * 学生取消预约（仅 PENDING 状态可取消）
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancelAppointment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        appointmentService.cancelAppointment(id, userId);
        return Result.success("预约已取消");
    }

    // --- 教师端接口 ---

    /**
     * 教师查看被预约记录列表
     */
    @GetMapping("/teacher/list")
    public Result<List<Appointment>> getTeacherAppointments(@RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<Appointment> list = appointmentService.getTeacherAppointments(userId, status);
        return Result.success(list);
    }

    /**
     * 教师审批/处理预约工单
     */
    @PostMapping("/teacher/process")
    public Result<String> processAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String status,
            @RequestParam(required = false) String feedback) {

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        if (appointmentService.processAppointment(appointmentId, status, feedback)) {
            return Result.success("处理成功");
        }
        return Result.error("处理失败");
    }
}
