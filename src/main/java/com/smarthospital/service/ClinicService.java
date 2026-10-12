package com.smarthospital.service;

import com.smarthospital.common.BizException;
import com.smarthospital.dto.Dtos.DoctorDetailVO;
import com.smarthospital.dto.Dtos.DoctorVO;
import com.smarthospital.dto.Dtos.ScheduleVO;
import com.smarthospital.entity.Department;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.Schedule;
import com.smarthospital.repository.DepartmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 科室 / 医生 / 排班查询 */
@Service
@RequiredArgsConstructor
public class ClinicService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<Department> listDepartments() {
        return departmentRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<DoctorVO> listDoctors(Long departmentId) {
        List<Doctor> doctors = departmentId == null
                ? doctorRepository.findAllByOrderByIdAsc()
                : doctorRepository.findByDepartmentIdOrderByIdAsc(departmentId);
        return doctors.stream().map(this::toVO).toList();
    }

    @Transactional(readOnly = true)
    public DoctorDetailVO doctorDetail(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BizException("医生不存在"));
        LocalDate today = LocalDate.now();
        List<ScheduleVO> schedules = scheduleRepository
                .findByDoctorIdAndDateBetweenOrderByDateAscPeriodAsc(id, today, today.plusDays(6))
                .stream().map(this::toVO).toList();
        return new DoctorDetailVO(toVO(doctor), schedules);
    }

    @Transactional(readOnly = true)
    public List<ScheduleVO> listSchedules(Long doctorId, LocalDate start, LocalDate end) {
        return scheduleRepository
                .findByDoctorIdAndDateBetweenOrderByDateAscPeriodAsc(doctorId, start, end)
                .stream().map(this::toVO).toList();
    }

    public DoctorVO toVO(Doctor doctor) {
        return new DoctorVO(
                doctor.getId(),
                doctor.getUser().getName(),
                doctor.getDepartment().getId(),
                doctor.getDepartment().getName(),
                doctor.getTitle(),
                doctor.getSpecialty(),
                doctor.getIntro(),
                doctor.getFee() == null ? BigDecimal.ZERO : doctor.getFee()
        );
    }

    public ScheduleVO toVO(Schedule schedule) {
        return new ScheduleVO(
                schedule.getId(),
                schedule.getDate(),
                schedule.getPeriod().name(),
                schedule.getCapacity(),
                schedule.getBooked(),
                Math.max(0, schedule.getCapacity() - schedule.getBooked())
        );
    }
}
