package com.hospital.appointments.service;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.dto.AppointmentUpdateRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentDto book(AppointmentCreateRequest request, Long patientId, String authorization);

    AppointmentDto reschedule(Long appointmentId, AppointmentUpdateRequest request, Long patientId, String authorization);

    List<AppointmentDto> getMyAppointments(Long patientId, String authorization);

    List<AppointmentDto> getDoctorAppointments(Long doctorId, String authorization);

    AppointmentDto updateStatus(Long appointmentId, AppointmentStatusUpdateRequest request, Long doctorId, String authorization);

    AppointmentDto getOwnedAppointment(Long appointmentId, Long userId, String role, String authorization);

    List<AppointmentDto> getAllAppointments(String authorization);
}

