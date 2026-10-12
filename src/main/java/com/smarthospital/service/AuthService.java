package com.smarthospital.service;

import com.smarthospital.common.BizException;
import com.smarthospital.common.TokenStore;
import com.smarthospital.dto.Dtos.LoginReq;
import com.smarthospital.dto.Dtos.LoginVO;
import com.smarthospital.dto.Dtos.ProfileUpdateReq;
import com.smarthospital.dto.Dtos.RegisterReq;
import com.smarthospital.dto.Dtos.UserVO;
import com.smarthospital.entity.User;
import com.smarthospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenStore tokenStore;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserVO register(RegisterReq req) {
        if (userRepository.existsByUsername(req.username().trim())) {
            throw new BizException("用户名已存在，请换个用户名");
        }
        User user = new User();
        user.setUsername(req.username().trim());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setName(req.name().trim());
        user.setPhone(req.phone());
        user.setGender(req.gender());
        user.setAge(req.age());
        user.setRole(User.Role.PATIENT);
        return toVO(userRepository.save(user));
    }

    public LoginVO login(LoginReq req) {
        User user = userRepository.findByUsername(req.username().trim())
                .orElseThrow(() -> new BizException("用户名或密码错误"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String token = tokenStore.create(user.getId());
        return new LoginVO(token, toVO(user));
    }

    public UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getName(), user.getPhone(),
                user.getRole().name(), user.getGender(), user.getAge(), user.getIdCard(), user.getAddress());
    }

    @Transactional
    public UserVO updateProfile(Long userId, ProfileUpdateReq req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (req.name() != null) user.setName(req.name().trim());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.gender() != null) user.setGender(req.gender());
        if (req.age() != null) user.setAge(req.age());
        if (req.idCard() != null) user.setIdCard(req.idCard());
        if (req.address() != null) user.setAddress(req.address());
        return toVO(userRepository.save(user));
    }
}
