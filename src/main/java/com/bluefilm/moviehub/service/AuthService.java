package com.bluefilm.moviehub.service;

import com.bluefilm.moviehub.model.User;
import com.bluefilm.moviehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthResult login(String username, String password) {
        User user = userRepository.findByUsername(normalize(username))
                .filter(item -> item.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码不正确"));
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, user.getId());
        return new AuthResult(token, user);
    }

    @Transactional
    public AuthResult register(String username, String password, String nickname) {
        String normalizedUsername = normalize(username);
        if (normalizedUsername.length() < 3) {
            throw new IllegalArgumentException("用户名至少需要 3 个字符");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码至少需要 6 个字符");
        }
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("这个用户名已经被使用");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(password);
        user.setNickname(nickname == null || nickname.isBlank() ? normalizedUsername : nickname.trim());
        user.setAvatarUrl("/assets/avatars/avatar-" + ((Math.abs(normalizedUsername.hashCode()) % 6) + 1) + ".jpg");
        user.setRole("USER");
        userRepository.save(user);
        return login(normalizedUsername, password);
    }

    public Optional<User> findByToken(String authorization) {
        String token = readToken(authorization);
        if (token == null) {
            return Optional.empty();
        }
        Long userId = sessions.get(token);
        return userId == null ? Optional.empty() : userRepository.findById(userId);
    }

    public User requireAdmin(String authorization) {
        User user = findByToken(authorization)
                .orElseThrow(() -> new IllegalArgumentException("请先登录管理员账号"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("当前账号没有管理权限");
        }
        return user;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String readToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String value = authorization.trim();
        return value.startsWith("Bearer ") ? value.substring(7) : value;
    }

    public record AuthResult(String token, User user) {
    }
}
