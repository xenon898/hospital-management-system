package com.hospital.doctors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotBlank(message = "specialization is required")
    private String specialization;
}

