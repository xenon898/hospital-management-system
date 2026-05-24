package com.hospital.doctors.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {

    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String content;
    private LocalDateTime createdAt;
}

