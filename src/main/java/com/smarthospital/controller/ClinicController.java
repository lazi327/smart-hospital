package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.dto.Dtos.DoctorDetailVO;
import com.smarthospital.dto.Dtos.DoctorVO;
import com.smarthospital.dto.Dtos.ScheduleVO;
import com.smarthospital.entity.Department;
import com.smarthospital.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 科室与医生（公开访问） */
@RestController
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping("/api/departments")
    public Result<List<Department>> departments() {
        return Result.ok(clinicService.listDepartments());
    }

    @GetMapping("/api/doctors")
    public Result<List<DoctorVO>> doctors(@RequestParam(required = false) Long departmentId) {
        return Result.ok(clinicService.listDoctors(departmentId));
    }

    @GetMapping("/api/doctors/{id}")
    public Result<DoctorDetailVO> doctorDetail(@PathVariable Long id) {
        return Result.ok(clinicService.doctorDetail(id));
    }

    @GetMapping("/api/schedules")
    public Result<List<ScheduleVO>> schedules(@RequestParam Long doctorId,
                                              @RequestParam String start,
                                              @RequestParam String end) {
        return Result.ok(clinicService.listSchedules(doctorId, LocalDate.parse(start), LocalDate.parse(end)));
    }
}
