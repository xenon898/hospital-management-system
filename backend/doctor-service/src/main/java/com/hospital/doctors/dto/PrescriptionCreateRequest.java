package com.hospital.doctors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionCreateRequest {

    @NotNull
    private Long appointmentId;

    @NotNull
    private Long patientId;

    @NotBlank
    private String content;
}

