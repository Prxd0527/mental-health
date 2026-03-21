package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.MoodLog;

import java.util.List;
import java.util.Map;

public interface MoodLogService extends IService<MoodLog> {

    /**
     * 每日心情打卡（同一天只能打卡一次，重复打卡覆盖）
     */
    boolean checkIn(MoodLog moodLog);

    /**
     * 获取用户某月的心情记录
     */
    List<MoodLog> getMonthlyLogs(Long userId, int year, int month);

    /**
     * 获取用户月度情绪统计（饼图/柱状图数据）
     */
    Map<String, Object> getMonthlyStats(Long userId, int year, int month);

    /**
     * 获取用户今日打卡记录
     */
    MoodLog getTodayLog(Long userId);
}
