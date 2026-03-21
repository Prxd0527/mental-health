package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.MoodLog;
import com.mentalhealth.service.MoodLogService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mood")
public class MoodLogController {

    @Resource
    private MoodLogService moodLogService;

    /**
     * 每日心情打卡
     */
    @PostMapping("/checkin")
    public Result<String> checkIn(@RequestBody MoodLog moodLog) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        moodLog.setUserId(userId);
        if (moodLogService.checkIn(moodLog)) {
            return Result.success("打卡成功！今天也要元气满满哦 ✨");
        }
        return Result.error("打卡失败");
    }

    /**
     * 获取今日打卡记录
     */
    @GetMapping("/today")
    public Result<MoodLog> getToday() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        MoodLog log = moodLogService.getTodayLog(userId);
        return Result.success(log);
    }

    /**
     * 获取某月的心情打卡记录
     */
    @GetMapping("/monthly")
    public Result<List<MoodLog>> getMonthly(
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        List<MoodLog> logs = moodLogService.getMonthlyLogs(userId, year, month);
        return Result.success(logs);
    }

    /**
     * 获取月度情绪统计（分布 + 趋势曲线 + 平均分）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        Map<String, Object> stats = moodLogService.getMonthlyStats(userId, year, month);
        return Result.success(stats);
    }
}
