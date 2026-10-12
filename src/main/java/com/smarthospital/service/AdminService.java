package com.smarthospital.service;

import com.smarthospital.common.BizException;
import com.smarthospital.dto.Dtos.DeptReq;
import com.smarthospital.dto.Dtos.DoctorReq;
import com.smarthospital.dto.Dtos.DoctorVO;
import com.smarthospital.dto.Dtos.ScheduleReq;
import com.smarthospital.dto.Dtos.ScheduleVO;
import com.smarthospital.entity.Department;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.Schedule;
import com.smarthospital.entity.User;
import com.smarthospital.repository.AppointmentRepository;
import com.smarthospital.repository.DepartmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.ScheduleRepository;
import com.smarthospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 管理后台：科室 / 医生 / 排班管理 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicService clinicService;

    // ---------- 科室 ----------
    @Transactional
    public Department saveDepartment(DeptReq req) {
        Department dept = req.id() == null ? new Department()
                : departmentRepository.findById(req.id()).orElseThrow(() -> new BizException("科室不存在"));
        dept.setName(req.name().trim());
        dept.setDescription(req.description());
        dept.setLocation(req.location());
        dept.setIcon(req.icon());
        return departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (doctorRepository.existsByDepartmentId(id)) {
            throw new BizException("该科室下存在医生，无法删除");
        }
        departmentRepository.deleteById(id);
    }

    // ---------- 医生 ----------
    @Transactional
    public DoctorVO saveDoctor(DoctorReq req) {
        Doctor doctor;
        if (req.id() == null) {
            if (req.username() == null || req.username().isBlank()
                    || req.password() == null || req.password().isBlank()) {
                throw new BizException("新医生必须设置登录账号和密码");
            }
            String username = req.username().trim();
            if (userRepository.existsByUsername(username)) {
                throw new BizException("登录账号已存在");
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(req.password()));
            user.setName(req.name().trim());
            user.setRole(User.Role.DOCTOR);
            user = userRepository.save(user);
            doctor = new Doctor();
            doctor.setUser(user);
        } else {
            doctor = doctorRepository.findById(req.id())
                    .orElseThrow(() -> new BizException("医生不存在"));
            User user = doctor.getUser();
            user.setName(req.name().trim());
            if (req.password() != null && !req.password().isBlank()) {
                user.setPassword(passwordEncoder.encode(req.password()));
            }
        }
        doctor.setDepartment(departmentRepository.getReferenceById(req.departmentId()));
        doctor.setTitle(req.title());
        doctor.setSpecialty(req.specialty());
        doctor.setIntro(req.intro());
        doctor.setFee(req.fee());
        return clinicService.toVO(doctorRepository.save(doctor));
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BizException("医生不存在"));
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new BizException("该医生存在挂号记录，无法删除");
        }
        scheduleRepository.deleteByDoctorId(id);
        doctorRepository.delete(doctor);
        userRepository.delete(doctor.getUser());
    }

    // ---------- 排班 ----------
    @Transactional
    public void upsertSchedule(ScheduleReq req) {
        if (!"AM".equals(req.period()) && !"PM".equals(req.period())) {
            throw new BizException("时段必须是 AM 或 PM");
        }
        Schedule.Period period = Schedule.Period.valueOf(req.period());
        Schedule schedule = scheduleRepository
                .findByDoctorIdAndDateAndPeriod(req.doctorId(), req.date(), period)
                .orElseGet(() -> {
                    Schedule s = new Schedule();
                    s.setDoctor(doctorRepository.getReferenceById(req.doctorId()));
                    s.setDate(req.date());
                    s.setPeriod(period);
                    s.setBooked(0);
                    return s;
                });
        schedule.setCapacity(req.capacity() == null ? 20 : Math.max(0, req.capacity()));
        scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleVO> schedules(Long doctorId, LocalDate start, LocalDate end) {
        return scheduleRepository.findByDoctorIdAndDateBetween(doctorId, start, end)
                .stream().map(clinicService::toVO).toList();
    }
}
