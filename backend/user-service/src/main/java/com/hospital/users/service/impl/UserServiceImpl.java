package com.hospital.users.service.impl;

import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.dto.RegisterRequest;
import com.hospital.users.dto.UserMeResponse;
import com.hospital.users.entity.AppUser;
import com.hospital.users.entity.Role;
import com.hospital.users.repository.AppUserRepository;
import com.hospital.users.security.JwtUtil;
import com.hospital.users.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(AppUserRepository appUserRepository, JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        AppUser saved = appUserRepository.save(user);

        String token = jwtUtil.generateToken(saved.getId().toString(), saved.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .role(saved.getRole().name())
                .userId(saved.getId().toString())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        boolean ok = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getId().toString(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId().toString())
                .build();
    }

    @Override
    public UserMeResponse me(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization Bearer token is required");
        }

        String token = authorization.substring("Bearer ".length());

        String subject = jwtUtil.getSubject(token);
        String role = jwtUtil.getRole(token);

        return UserMeResponse.builder()
                .userId(subject)
                .role(role)
                .build();
    }
}

