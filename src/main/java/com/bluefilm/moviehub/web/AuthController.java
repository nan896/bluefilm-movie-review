package com.bluefilm.moviehub.web;

import com.bluefilm.moviehub.model.User;
import com.bluefilm.moviehub.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request.username(), request.password());
        return ApiResponse.ok(toSession(result.token(), result.user()));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request.username(), request.password(), request.nickname());
        return ApiResponse.ok("注册成功", toSession(result.token(), result.user()));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.findByToken(authorization)
                .map(user -> ApiResponse.ok(toSession(null, user)))
                .orElseGet(() -> ApiResponse.fail("尚未登录"));
    }

    private Map<String, Object> toSession(String token, User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("role", user.getRole());
        return data;
    }

    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String password, String nickname) {
    }
}
