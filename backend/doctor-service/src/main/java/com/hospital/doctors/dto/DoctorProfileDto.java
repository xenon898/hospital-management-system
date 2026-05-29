package com.hospital.doctors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfileDto {

    private Long id;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    private String name;

    @NotBlank(message = "specialization is required")
    @Size(min = 2, max = 120, message = "specialization must be between 2 and 120 characters")
    private String specialization;
}

