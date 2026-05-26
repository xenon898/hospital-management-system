package com.hospital.users.controller;

import com.hospital.users.dto.AdminCreateDoctorRequest;
import com.hospital.users.dto.AdminCreatePatientRequest;
import com.hospital.users.dto.AdminCreateUserResponse;
import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.LoginRequest;
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

    @PostMapping("/admin/create-doctor")
    public ResponseEntity<AdminCreateUserResponse> createDoctor(@Valid @RequestBody AdminCreateDoctorRequest request,
                                                                @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(userService.createDoctor(request, authorization));
    }

    @PostMapping("/admin/create-patient")
    public ResponseEntity<AdminCreateUserResponse> createPatient(@Valid @RequestBody AdminCreatePatientRequest request,
                                                                 @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(userService.createPatient(request, authorization));
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

