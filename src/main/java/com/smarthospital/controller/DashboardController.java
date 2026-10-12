package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.DashboardVO;
import com.smarthospital.entity.User;
import com.smarthospital.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard/stats")
    public Result<DashboardVO> stats() {
        UserContext.requireRole(User.Role.ADMIN);
        return Result.ok(dashboardService.stats());
    }
}
