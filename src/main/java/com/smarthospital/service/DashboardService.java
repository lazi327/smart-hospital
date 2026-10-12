package com.smarthospital.service;

import com.smarthospital.dto.Dtos.DashboardVO;
import com.smarthospital.dto.Dtos.DayStat;
import com.smarthospital.dto.Dtos.DeptStat;
import com.smarthospital.entity.Appointment;
import com.smarthospital.repository.AppointmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.UserRepository;
import com.smarthospital.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public DashboardVO stats() {
        LocalDate today = LocalDate.now();
        long todayCount = appointmentRepository.countByDateAndStatusNot(today, Appointment.Status.CANCELLED);
        long patients = userRepository.countByRole(User.Role.PATIENT);
        long doctors = doctorRepository.count();

        // 近 30 天科室分布
        List<Appointment> recent = appointmentRepository.findByDateBetween(today.minusDays(29), today);
        Map<String, Long> byDept = recent.stream()
                .filter(a -> a.getStatus() != Appointment.Status.CANCELLED)
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getDepartment().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        List<DeptStat> deptDistribution = byDept.entrySet().stream()
                .map(e -> new DeptStat(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();

        // 近 7 天挂号趋势
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> byDate = recent.stream()
                .filter(a -> !a.getDate().isBefore(today.minusDays(6)))
                .filter(a -> a.getStatus() != Appointment.Status.CANCELLED)
                .collect(Collectors.groupingBy(a -> a.getDate().format(fmt), Collectors.counting()));
        List<DayStat> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String key = today.minusDays(i).format(fmt);
            trend.add(new DayStat(key, byDate.getOrDefault(key, 0L)));
        }

        return new DashboardVO(todayCount, patients, doctors, deptDistribution, trend);
    }
}
