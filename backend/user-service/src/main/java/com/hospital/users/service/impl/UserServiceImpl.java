package com.hospital.users.service.impl;

import com.hospital.users.dto.AdminCreateDoctorRequest;
import com.hospital.users.dto.AdminCreatePatientRequest;
import com.hospital.users.dto.AdminCreateUserResponse;
import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.dto.ProfileCreateResponse;
import com.hospital.users.dto.UserMeResponse;
import com.hospital.users.entity.AppUser;
import com.hospital.users.entity.Role;
import com.hospital.users.repository.AppUserRepository;
import com.hospital.users.security.JwtUtil;
import com.hospital.users.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${services.doctor.url}")
    private String doctorUrl;

    @Value("${services.patient.url}")
    private String patientUrl;

    public UserServiceImpl(AppUserRepository appUserRepository, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.appUserRepository = appUserRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public AdminCreateUserResponse createDoctor(AdminCreateDoctorRequest request, String authorization) {
        AppUser user = createUser(request.getUsername(), request.getPassword(), Role.DOCTOR);
        ProfileCreateResponse profile = createProfile(
                doctorUrl + "/api/doctors",
                Map.of(
                        "userId", user.getId(),
                        "name", request.getName().trim(),
                        "specialization", request.getSpecialization().trim()
                ),
                authorization
        );

        return AdminCreateUserResponse.builder()
                .userId(user.getId())
                .profileId(profile.getId())
                .message("Doctor account and profile created successfully")
                .build();
    }

    @Override
    @Transactional
    public AdminCreateUserResponse createPatient(AdminCreatePatientRequest request, String authorization) {
        AppUser user = createUser(request.getUsername(), request.getPassword(), Role.PATIENT);
        Map<String, Object> profileBody = new HashMap<>();
        profileBody.put("userId", user.getId());
        profileBody.put("name", request.getName().trim());
        if (request.getAge() != null) {
            profileBody.put("age", request.getAge());
        }
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        if (phone != null && !phone.isBlank()) {
            if (phone.chars().distinct().count() == 1 || "1234567890".equals(phone)) {
                throw new IllegalArgumentException("Phone must be a realistic 10 digit mobile number");
            }
            profileBody.put("phone", phone);
        }

        ProfileCreateResponse profile = createProfile(
                patientUrl + "/api/patients",
                profileBody,
                authorization
        );

        return AdminCreateUserResponse.builder()
                .userId(user.getId())
                .profileId(profile.getId())
                .message("Patient account and profile created successfully")
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

    private AppUser createUser(String username, String password, Role role) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (appUserRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = AppUser.builder()
                .username(cleanUsername)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();

        // saveAndFlush gives us the generated ID before calling the profile service.
        return appUserRepository.saveAndFlush(user);
    }

    private ProfileCreateResponse createProfile(String url, Map<String, ?> body, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ProfileCreateResponse response = restTemplate.postForObject(
                    url,
                    new HttpEntity<>(body, headers),
                    ProfileCreateResponse.class
            );
            if (response == null || response.getId() == null) {
                throw new IllegalArgumentException("Profile service did not return a profile ID");
            }
            return response;
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Profile creation failed: " + ex.getMessage());
        }
    }
}

