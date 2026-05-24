package com.hospital.users.controller;

import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.dto.RegisterRequest;
import com.hospital.users.dto.UserMeResponse;
import com.hospital.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * Beginner-friendly endpoint: returns token subject and role.
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(userService.me(authorization));
    }
}

