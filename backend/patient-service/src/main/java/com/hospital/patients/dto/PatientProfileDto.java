package com.hospital.patients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileDto {
    private Long id;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    private String name;

    @Min(value = 1, message = "Age must be greater than 0")
    @Max(value = 120, message = "Invalid age entered")
    private Integer age;

    @Size(max = 10, message = "phone must be exactly 10 digits")
    private String phone;
}

