package com.smarthospital.controller;

import com.smarthospital.common.Result;
import com.smarthospital.common.TokenStore;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.LoginReq;
import com.smarthospital.dto.Dtos.LoginVO;
import com.smarthospital.dto.Dtos.ProfileUpdateReq;
import com.smarthospital.dto.Dtos.RegisterReq;
import com.smarthospital.dto.Dtos.UserVO;
import com.smarthospital.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenStore tokenStore;

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid RegisterReq req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginReq req) {
        return Result.ok(authService.login(req));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.toVO(UserContext.get()));
    }

    @PutMapping("/me")
    public Result<UserVO> updateMe(@RequestBody ProfileUpdateReq req) {
        return Result.ok(authService.updateProfile(UserContext.get().getId(), req));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        tokenStore.remove(token);
        return Result.ok();
    }
}
