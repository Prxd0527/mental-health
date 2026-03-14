package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.TeacherSchedule;

import java.time.LocalDate;
import java.util.List;

public interface TeacherScheduleService extends IService<TeacherSchedule> {

    /**
     * 教师发布或更新某天的排班记录
     *
     * @param schedule 排班实体（包含日期与以逗号分隔的可用时间段）
     */
    boolean saveOrUpdateSchedule(TeacherSchedule schedule);

    /**
     * 学生或系统查询某位教师从指定日期开始的近期排班列表
     *
     * @param teacherId 教师ID
     * @param startDate 起始日期，通常为今天
     * @param days      往后查询的天数
     */
    List<TeacherSchedule> getTeacherSchedules(Long teacherId, LocalDate startDate, int days);

    /**
     * 教师查阅自己从某日开始的排班列表
     */
    List<TeacherSchedule> getMySchedules(Long teacherId, LocalDate startDate, int days);
}
