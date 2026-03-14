package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Appointment;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {

    /**
     * 学生发起预约申请
     */
    boolean submitAppointment(Appointment appointment);

    /**
     * 获取指定学生的预约记录列表
     */
    List<Appointment> getStudentAppointments(Long studentId);

    /**
     * 获取指定咨询师的被预约记录列表
     */
    List<Appointment> getTeacherAppointments(Long teacherId, String status);

    /**
     * 咨询师审批/处理预约工单
     *
     * @param appointmentId 预约单ID
     * @param status        处理状态 (APPROVED, REJECTED, COMPLETED 等)
     * @param feedback      反馈意见
     */
    boolean processAppointment(Long appointmentId, String status, String feedback);

    /**
     * 学生取消预约（仅 PENDING 状态可取消）
     *
     * @param appointmentId 预约单ID
     * @param studentId     操作者学生ID（用于校验本人操作）
     */
    void cancelAppointment(Long appointmentId, Long studentId);
}
