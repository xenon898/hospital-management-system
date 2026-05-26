package com.hospital.users.service;

import com.hospital.users.dto.AuthResponse;
import com.hospital.users.dto.AdminCreateDoctorRequest;
import com.hospital.users.dto.AdminCreatePatientRequest;
import com.hospital.users.dto.AdminCreateUserResponse;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.dto.UserMeResponse;

public interface UserService {

    AdminCreateUserResponse createDoctor(AdminCreateDoctorRequest request, String authorization);

    AdminCreateUserResponse createPatient(AdminCreatePatientRequest request, String authorization);

    AuthResponse login(LoginRequest request);

    UserMeResponse me(String authorization);
}

