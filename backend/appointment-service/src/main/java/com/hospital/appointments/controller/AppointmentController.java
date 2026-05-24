package com.hospital.appointments.controller;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** PATIENT: book appointment */
    @PostMapping
    public AppointmentDto book(@RequestBody @Valid AppointmentCreateRequest request,
                                 Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return appointmentService.book(request, patientId);
    }

    /** PATIENT: list my appointments */
    @GetMapping("/my")
    public List<AppointmentDto> myAppointments(Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return appointmentService.getMyAppointments(patientId);
    }

    /** DOCTOR: list appointments */
    @GetMapping("/doctor")
    public List<AppointmentDto> doctorAppointments(Authentication authentication) {
        Long doctorId = Long.valueOf(authentication.getName());
        return appointmentService.getDoctorAppointments(doctorId);
    }

    /** DOCTOR: update status */
    @PatchMapping("/{appointmentId}/status")
    public AppointmentDto updateStatus(@PathVariable("appointmentId") Long appointmentId,
                                          @RequestBody @Valid AppointmentStatusUpdateRequest request) {
        return appointmentService.updateStatus(appointmentId, request);
    }

    /** ADMIN: view all */
    @GetMapping
    public List<AppointmentDto> all(Authentication authentication) {
        return appointmentService.getAllAppointments();
    }
}

