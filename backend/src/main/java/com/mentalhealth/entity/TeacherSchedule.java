package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("teacher_schedule")
public class TeacherSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private LocalDate workDate; // 开放预约的日期

    // 存储以逗号分隔的可用时间段，如 "09:00-10:00,10:30-11:30"
    // 或者存储 JSON 数组
    private String availableSlots;
}
