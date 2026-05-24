package com.hospital.appointments.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentCreateRequest {

    /** User id of the doctor account, used by the doctor's authenticated appointment lookup. */
    @NotNull
    private Long doctorId;

    @NotNull
    @Future(message = "appointmentTime must be in the future")
    private LocalDateTime appointmentTime;
}

