package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.TeacherSchedule;
import com.mentalhealth.service.TeacherScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class TeacherScheduleController {

    @Resource
    private TeacherScheduleService scheduleService;

    @PostMapping("/publish")
    public Result<String> publishSchedule(@RequestBody TeacherSchedule schedule) {
        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        // 此处实际上应校验该用户是否有教师权限
        schedule.setTeacherId(userId);
        if (scheduleService.saveOrUpdateSchedule(schedule)) {
            return Result.success("排班发布/更新成功");
        }
        return Result.error("排班操作失败");
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<TeacherSchedule>> getTeacherSchedules(
            @PathVariable Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "14") int days) {

        if (startDate == null) {
            startDate = LocalDate.now();
        }

        List<TeacherSchedule> schedules = scheduleService.getTeacherSchedules(teacherId, startDate, days);
        return Result.success(schedules);
    }

    @GetMapping("/my")
    public Result<List<TeacherSchedule>> getMySchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "14") int days) {

        Long userId = getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        List<TeacherSchedule> schedules = scheduleService.getMySchedules(userId, startDate, days);
        return Result.success(schedules);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal(); // 在Filter中将id存为了Principal
        }
        return null;
    }
}
