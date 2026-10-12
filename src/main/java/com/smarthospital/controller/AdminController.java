package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.DeptReq;
import com.smarthospital.dto.Dtos.DoctorReq;
import com.smarthospital.dto.Dtos.DoctorVO;
import com.smarthospital.dto.Dtos.ScheduleReq;
import com.smarthospital.dto.Dtos.ScheduleVO;
import com.smarthospital.entity.User;
import com.smarthospital.service.AdminService;
import com.smarthospital.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 管理后台接口（仅管理员） */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ClinicService clinicService;

    // ---------- 科室 ----------
    @GetMapping("/departments")
    public Result<?> departments() {
        UserContext.requireRole(User.Role.ADMIN);
        return Result.ok(clinicService.listDepartments());
    }

    @PostMapping("/departments")
    public Result<?> saveDepartment(@RequestBody @Valid DeptReq req) {
        UserContext.requireRole(User.Role.ADMIN);
        return Result.ok(adminService.saveDepartment(req));
    }

    @DeleteMapping("/departments/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        UserContext.requireRole(User.Role.ADMIN);
        adminService.deleteDepartment(id);
        return Result.ok();
    }

    // ---------- 医生 ----------
    @GetMapping("/doctors")
    public Result<List<DoctorVO>> doctors(@RequestParam(required = false) Long departmentId) {
        UserContext.requireRole(User.Role.ADMIN);
        return Result.ok(clinicService.listDoctors(departmentId));
    }

    @PostMapping("/doctors")
    public Result<DoctorVO> saveDoctor(@RequestBody @Valid DoctorReq req) {
        UserContext.requireRole(User.Role.ADMIN);
        return Result.ok(adminService.saveDoctor(req));
    }

    @DeleteMapping("/doctors/{id}")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        UserContext.requireRole(User.Role.ADMIN);
        adminService.deleteDoctor(id);
        return Result.ok();
    }

    // ---------- 排班 ----------
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> schedules(@RequestParam Long doctorId,
                                              @RequestParam(required = false) LocalDate start,
                                              @RequestParam(required = false) LocalDate end) {
        UserContext.requireRole(User.Role.ADMIN);
        LocalDate s = start == null ? LocalDate.now() : start;
        LocalDate e = end == null ? s.plusDays(6) : end;
        return Result.ok(adminService.schedules(doctorId, s, e));
    }

    @PostMapping("/schedules")
    public Result<Void> upsertSchedule(@RequestBody @Valid ScheduleReq req) {
        UserContext.requireRole(User.Role.ADMIN);
        adminService.upsertSchedule(req);
        return Result.ok();
    }
}
