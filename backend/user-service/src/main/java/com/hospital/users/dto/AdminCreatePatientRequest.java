package com.hospital.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreatePatientRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 60, message = "username must be between 3 and 60 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    private String name;

    @Min(value = 1, message = "Age must be greater than 0")
    @Max(value = 120, message = "Invalid age entered")
    private Integer age;

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$", message = "phone must be exactly 10 digits and start with 6, 7, 8, or 9")
    private String phone;
}
