package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.RecordReq;
import com.smarthospital.dto.Dtos.RecordVO;
import com.smarthospital.entity.User;
import com.smarthospital.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    public Result<RecordVO> create(@RequestBody @Valid RecordReq req) {
        UserContext.requireRole(User.Role.DOCTOR);
        return Result.ok(recordService.create(UserContext.get().getId(), req));
    }

    @GetMapping("/mine")
    public Result<List<RecordVO>> mine() {
        UserContext.requireRole(User.Role.PATIENT);
        return Result.ok(recordService.mine(UserContext.get().getId()));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public Result<RecordVO> byAppointment(@PathVariable Long appointmentId) {
        User user = UserContext.get();
        UserContext.requireRole(User.Role.DOCTOR, User.Role.PATIENT);
        return Result.ok(recordService.findByAppointment(appointmentId, user));
    }
}
