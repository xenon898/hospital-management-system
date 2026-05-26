package com.hospital.patients.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PrescriptionDto {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String content;
    private LocalDateTime createdAt;
}
