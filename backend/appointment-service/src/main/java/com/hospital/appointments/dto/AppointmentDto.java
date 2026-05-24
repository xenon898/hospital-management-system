package com.hospital.appointments.dto;

import com.hospital.appointments.entity.AppointmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {

    private Long id;
    private Long doctorId;
    private Long patientId;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
}

