package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.MoodLog;
import com.mentalhealth.mapper.MoodLogMapper;
import com.mentalhealth.service.MoodLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MoodLogServiceImpl extends ServiceImpl<MoodLogMapper, MoodLog> implements MoodLogService {

    // 心情等级映射(用于趋势图数值化)
    private static final Map<String, Integer> MOOD_SCORE = new LinkedHashMap<>();
    static {
        MOOD_SCORE.put("TERRIBLE", 1);
        MOOD_SCORE.put("SAD", 2);
        MOOD_SCORE.put("NEUTRAL", 3);
        MOOD_SCORE.put("GOOD", 4);
        MOOD_SCORE.put("HAPPY", 5);
    }

    @Override
    public boolean checkIn(MoodLog moodLog) {
        if (moodLog.getMood() == null || moodLog.getEmoji() == null) {
            throw new RuntimeException("请选择今日心情");
        }

        // 设置打卡日期为今天
        moodLog.setLogDate(LocalDate.now());

        // 检查今天是否已打卡
        MoodLog existing = getTodayLog(moodLog.getUserId());
        if (existing != null) {
            // 覆盖更新
            existing.setMood(moodLog.getMood());
            existing.setEmoji(moodLog.getEmoji());
            existing.setNote(moodLog.getNote());
            return this.updateById(existing);
        }

        return this.save(moodLog);
    }

    @Override
    public List<MoodLog> getMonthlyLogs(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return baseMapper.selectList(new LambdaQueryWrapper<MoodLog>()
                .eq(MoodLog::getUserId, userId)
                .ge(MoodLog::getLogDate, start)
                .le(MoodLog::getLogDate, end)
                .orderByAsc(MoodLog::getLogDate));
    }

    @Override
    public Map<String, Object> getMonthlyStats(Long userId, int year, int month) {
        List<MoodLog> logs = getMonthlyLogs(userId, year, month);

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 打卡天数
        result.put("totalDays", logs.size());

        // 2. 心情分布（饼图数据）
        Map<String, Long> distribution = logs.stream()
                .collect(Collectors.groupingBy(MoodLog::getMood, Collectors.counting()));
        result.put("distribution", distribution);

        // 3. 情绪趋势（折线图数据: [{date, score, mood, emoji}] ）
        List<Map<String, Object>> trend = new ArrayList<>();
        for (MoodLog log : logs) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", log.getLogDate().toString());
            point.put("score", MOOD_SCORE.getOrDefault(log.getMood(), 3));
            point.put("mood", log.getMood());
            point.put("emoji", log.getEmoji());
            trend.add(point);
        }
        result.put("trend", trend);

        // 4. 平均心情分数
        if (!logs.isEmpty()) {
            double avg = logs.stream()
                    .mapToInt(l -> MOOD_SCORE.getOrDefault(l.getMood(), 3))
                    .average().orElse(3.0);
            result.put("avgScore", Math.round(avg * 10) / 10.0);
        } else {
            result.put("avgScore", 0);
        }

        return result;
    }

    @Override
    public MoodLog getTodayLog(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<MoodLog>()
                .eq(MoodLog::getUserId, userId)
                .eq(MoodLog::getLogDate, LocalDate.now()));
    }
}
