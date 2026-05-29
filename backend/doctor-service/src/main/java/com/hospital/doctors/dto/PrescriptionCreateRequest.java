package com.hospital.doctors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionCreateRequest {

    @NotNull
    private Long appointmentId;

    /** Optional legacy field; patient ownership is resolved from the appointment service. */
    private Long patientId;

    @NotBlank(message = "Prescription content is required")
    @Size(min = 5, max = 2000, message = "Prescription content must be between 5 and 2000 characters")
    private String content;
}

