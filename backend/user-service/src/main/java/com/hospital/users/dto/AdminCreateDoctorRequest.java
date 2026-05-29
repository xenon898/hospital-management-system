package com.hospital.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateDoctorRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 60, message = "username must be between 3 and 60 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    private String name;

    @NotBlank(message = "specialization is required")
    @Size(min = 2, max = 120, message = "specialization must be between 2 and 120 characters")
    private String specialization;
}
