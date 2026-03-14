package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    // --- 学生端接口 ---

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Appointment appointment) {
        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        appointment.setStudentId(userId);
        if (appointmentService.submitAppointment(appointment)) {
            return Result.success("预约申请已提交");
        }
        return Result.error("预约失败");
    }

    @GetMapping("/my")
    public Result<List<Appointment>> getMyAppointments() {
        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<Appointment> list = appointmentService.getStudentAppointments(userId);
        return Result.success(list);
    }

    // --- 教师端接口 ---

    @GetMapping("/teacher/list")
    public Result<List<Appointment>> getTeacherAppointments(@RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<Appointment> list = appointmentService.getTeacherAppointments(userId, status);
        return Result.success(list);
    }

    @PostMapping("/teacher/process")
    public Result<String> processAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String status,
            @RequestParam(required = false) String feedback) {

        // 此处为了安全起见可校验操作者是否为该预约记录的教师，这里略作简化
        if (appointmentService.processAppointment(appointmentId, status, feedback)) {
            return Result.success("处理成功");
        }
        return Result.error("处理失败");
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
