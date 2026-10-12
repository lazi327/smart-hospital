package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.AppointmentVO;
import com.smarthospital.dto.Dtos.BookReq;
import com.smarthospital.entity.User;
import com.smarthospital.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public Result<AppointmentVO> book(@RequestBody @Valid BookReq req) {
        UserContext.requireRole(User.Role.PATIENT);
        return Result.ok(appointmentService.book(UserContext.get().getId(), req.scheduleId()));
    }

    @GetMapping("/mine")
    public Result<List<AppointmentVO>> mine() {
        UserContext.requireRole(User.Role.PATIENT);
        return Result.ok(appointmentService.mine(UserContext.get().getId()));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        UserContext.requireRole(User.Role.PATIENT);
        appointmentService.cancel(UserContext.get().getId(), id);
        return Result.ok();
    }

    @GetMapping("/today")
    public Result<List<AppointmentVO>> today() {
        UserContext.requireRole(User.Role.DOCTOR);
        return Result.ok(appointmentService.todayForDoctor(UserContext.get().getId()));
    }

    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        UserContext.requireRole(User.Role.DOCTOR);
        appointmentService.finish(UserContext.get().getId(), id);
        return Result.ok();
    }
}
