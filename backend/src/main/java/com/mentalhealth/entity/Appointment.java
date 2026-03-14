package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long teacherId;

    private LocalDate appointDate;
    private String timeSlot; // 格式如 09:00-10:00
    private String requirement;

    // PENDING-待审核, APPROVED-已同意, REJECTED-已拒绝, COMPLETED-已完成, CANCELED-已取消
    private String status;
    private String feedback; // 审批反馈/拒绝理由

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String studentName; // 关联展示

    @TableField(exist = false)
    private String teacherName; // 关联展示
}
