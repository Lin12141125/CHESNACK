package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.LoginDTO;
import com.chesnake.dto.RegisterDTO;
import com.chesnake.security.LoginUser;
import com.chesnake.security.TokenBlacklistService;
import com.chesnake.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return R.ok();
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public R<LoginUser> me() {
        return R.ok(authService.me());
    }

    /** 退出登录：token 加入 Redis 黑名单，实现主动吊销 */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authorization.substring(7));
        }
        return R.ok();
    }
}
