package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.TeacherSchedule;
import com.mentalhealth.mapper.TeacherScheduleMapper;
import com.mentalhealth.service.TeacherScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TeacherScheduleServiceImpl extends ServiceImpl<TeacherScheduleMapper, TeacherSchedule>
        implements TeacherScheduleService {

    @Override
    public boolean saveOrUpdateSchedule(TeacherSchedule schedule) {
        if (schedule.getTeacherId() == null || schedule.getWorkDate() == null) {
            throw new RuntimeException("教师ID或排期日期不能为空");
        }

        // 查找是否该日已存在排班，存在则覆盖更新，否则新建
        TeacherSchedule existing = baseMapper.selectOne(new LambdaQueryWrapper<TeacherSchedule>()
                .eq(TeacherSchedule::getTeacherId, schedule.getTeacherId())
                .eq(TeacherSchedule::getWorkDate, schedule.getWorkDate()));

        if (existing != null) {
            existing.setAvailableSlots(schedule.getAvailableSlots());
            return this.updateById(existing);
        } else {
            return this.save(schedule);
        }
    }

    @Override
    public List<TeacherSchedule> getTeacherSchedules(Long teacherId, LocalDate startDate, int days) {
        LocalDate endDate = startDate.plusDays(days);
        return baseMapper.selectList(new LambdaQueryWrapper<TeacherSchedule>()
                .eq(TeacherSchedule::getTeacherId, teacherId)
                .ge(TeacherSchedule::getWorkDate, startDate)
                .lt(TeacherSchedule::getWorkDate, endDate)
                .orderByAsc(TeacherSchedule::getWorkDate));
    }

    @Override
    public List<TeacherSchedule> getMySchedules(Long teacherId, LocalDate startDate, int days) {
        return getTeacherSchedules(teacherId, startDate, days);
    }
}
