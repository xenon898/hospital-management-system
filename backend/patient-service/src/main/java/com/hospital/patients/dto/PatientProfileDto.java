package com.hospital.patients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    private Integer age;
    private String phone;
}

