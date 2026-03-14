package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.AppointmentMapper;
import com.mentalhealth.service.AppointmentService;
import com.mentalhealth.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Resource
    private UserService userService;

    @Override
    public boolean submitAppointment(Appointment appointment) {
        if (appointment.getTeacherId() == null || appointment.getAppointDate() == null
                || StrUtil.isBlank(appointment.getTimeSlot())) {
            throw new RuntimeException("预约信息不完整");
        }

        // 可选：检查该时段是否已被预约(冲突检测)
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTeacherId, appointment.getTeacherId())
                .eq(Appointment::getAppointDate, appointment.getAppointDate())
                .eq(Appointment::getTimeSlot, appointment.getTimeSlot())
                .in(Appointment::getStatus, "PENDING", "APPROVED"));

        if (count > 0) {
            throw new RuntimeException("该咨询师的此时段已被预约或正在审核中");
        }

        appointment.setStatus("PENDING");
        return this.save(appointment);
    }

    @Override
    public List<Appointment> getStudentAppointments(Long studentId) {
        List<Appointment> list = baseMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStudentId, studentId)
                .orderByDesc(Appointment::getCreateTime));

        // 填充咨询师姓名供前端展示
        for (Appointment appt : list) {
            User teacher = userService.getById(appt.getTeacherId());
            if (teacher != null) {
                appt.setTeacherName(
                        StrUtil.isNotBlank(teacher.getRealName()) ? teacher.getRealName() : teacher.getUsername());
            }
        }
        return list;
    }

    @Override
    public List<Appointment> getTeacherAppointments(Long teacherId, String status) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getTeacherId, teacherId);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getAppointDate, Appointment::getCreateTime);

        List<Appointment> list = baseMapper.selectList(wrapper);
        for (Appointment appt : list) {
            User student = userService.getById(appt.getStudentId());
            if (student != null) {
                appt.setStudentName(
                        StrUtil.isNotBlank(student.getRealName()) ? student.getRealName() : student.getUsername());
            }
        }
        return list;
    }

    @Override
    public boolean processAppointment(Long appointmentId, String status, String feedback) {
        Appointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        appointment.setStatus(status);
        appointment.setFeedback(feedback);
        return this.updateById(appointment);
    }
}
