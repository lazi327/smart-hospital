package com.smarthospital.service;

import com.smarthospital.common.BizException;
import com.smarthospital.dto.Dtos.AppointmentVO;
import com.smarthospital.entity.Appointment;
import com.smarthospital.entity.Schedule;
import com.smarthospital.entity.User;
import com.smarthospital.repository.AppointmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.ScheduleRepository;
import com.smarthospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    /** 挂号：原子占号，防止超卖 */
    @Transactional
    public AppointmentVO book(Long patientId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BizException("排班不存在"));
        if (schedule.getDate().isBefore(LocalDate.now())) {
            throw new BizException("该排班日期已过期");
        }
        int updated = scheduleRepository.tryBook(scheduleId);
        if (updated == 0) {
            throw new BizException("手慢了，该时段号源已满，请选择其他时段");
        }
        Integer bookedNow = scheduleRepository.getBookedById(scheduleId);

        Appointment appointment = new Appointment();
        appointment.setPatient(userRepository.getReferenceById(patientId));
        appointment.setDoctor(schedule.getDoctor());
        appointment.setSchedule(schedule);
        appointment.setDate(schedule.getDate());
        appointment.setPeriod(schedule.getPeriod());
        appointment.setQueueNo(bookedNow == null ? 1 : bookedNow);
        appointment.setStatus(Appointment.Status.BOOKED);
        return toVO(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentVO> mine(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toVO).toList();
    }

    /** 医生当日接诊列表 */
    @Transactional(readOnly = true)
    public List<AppointmentVO> todayForDoctor(Long doctorUserId) {
        Long doctorId = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new BizException("医生信息不存在"))
                .getId();
        return appointmentRepository
                .findByDoctorIdAndDateOrderByPeriodAscIdAsc(doctorId, LocalDate.now())
                .stream().map(this::toVO).toList();
    }

    @Transactional
    public void cancel(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BizException("挂号单不存在"));
        if (!appointment.getPatient().getId().equals(userId)) {
            throw new BizException(403, "只能取消本人挂号");
        }
        if (appointment.getStatus() != Appointment.Status.BOOKED) {
            throw new BizException("当前状态无法取消");
        }
        appointment.setStatus(Appointment.Status.CANCELLED);
        scheduleRepository.release(appointment.getSchedule().getId());
    }

    @Transactional
    public void finish(Long doctorUserId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BizException("挂号单不存在"));
        if (appointment.getStatus() != Appointment.Status.BOOKED) {
            throw new BizException("当前状态无法完成");
        }
        appointment.setStatus(Appointment.Status.FINISHED);
    }

    public AppointmentVO toVO(Appointment a) {
        return new AppointmentVO(
                a.getId(),
                a.getPatient().getName(),
                a.getDoctor().getUser().getName(),
                a.getDoctor().getTitle(),
                a.getDoctor().getDepartment().getName(),
                a.getDate(),
                a.getPeriod().name(),
                a.getQueueNo(),
                a.getStatus().name(),
                a.getDoctor().getFee(),
                a.getCreatedAt()
        );
    }
}
