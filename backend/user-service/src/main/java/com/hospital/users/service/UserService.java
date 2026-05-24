package com.hospital.users.service;

import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.dto.RegisterRequest;
import com.hospital.users.dto.UserMeResponse;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserMeResponse me(String authorization);
}

