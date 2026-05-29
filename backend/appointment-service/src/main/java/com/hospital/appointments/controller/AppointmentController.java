package com.hospital.appointments.controller;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.dto.AppointmentUpdateRequest;
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
                                 @RequestHeader("Authorization") String authorization,
                                 Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return appointmentService.book(request, patientId, authorization);
    }

    /** PATIENT: reschedule/change doctor while appointment is still pending. */
    @PutMapping("/{appointmentId}")
    public AppointmentDto reschedule(@PathVariable("appointmentId") Long appointmentId,
                                     @RequestBody @Valid AppointmentUpdateRequest request,
                                     @RequestHeader("Authorization") String authorization,
                                     Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return appointmentService.reschedule(appointmentId, request, patientId, authorization);
    }

    /** PATIENT: list my appointments */
    @GetMapping("/my")
    public List<AppointmentDto> myAppointments(@RequestHeader("Authorization") String authorization,
                                               Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return appointmentService.getMyAppointments(patientId, authorization);
    }

    /** DOCTOR: list appointments */
    @GetMapping("/doctor")
    public List<AppointmentDto> doctorAppointments(@RequestHeader("Authorization") String authorization,
                                                   Authentication authentication) {
        Long doctorId = Long.valueOf(authentication.getName());
        return appointmentService.getDoctorAppointments(doctorId, authorization);
    }

    /** DOCTOR: update status */
    @PatchMapping("/{appointmentId}/status")
    public AppointmentDto updateStatus(@PathVariable("appointmentId") Long appointmentId,
                                          @RequestBody @Valid AppointmentStatusUpdateRequest request,
                                          @RequestHeader("Authorization") String authorization,
                                          Authentication authentication) {
        Long doctorId = Long.valueOf(authentication.getName());
        return appointmentService.updateStatus(appointmentId, request, doctorId, authorization);
    }

    /** Authenticated lookup used by UI and prescription ownership validation. */
    @GetMapping("/{appointmentId}")
    public AppointmentDto getAppointment(@PathVariable("appointmentId") Long appointmentId,
                                         @RequestHeader("Authorization") String authorization,
                                         Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");
        return appointmentService.getOwnedAppointment(appointmentId, userId, role, authorization);
    }

    /** ADMIN: view all */
    @GetMapping
    public List<AppointmentDto> all(@RequestHeader("Authorization") String authorization,
                                    Authentication authentication) {
        return appointmentService.getAllAppointments(authorization);
    }
}

