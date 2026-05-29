package com.hospital.appointments.dto;

import com.hospital.appointments.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentStatusUpdateRequest {

    @NotNull(message = "status is required")
    private AppointmentStatus status;
}

