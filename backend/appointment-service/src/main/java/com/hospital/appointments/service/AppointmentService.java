package com.hospital.appointments.service;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentDto book(AppointmentCreateRequest request, Long patientId);

    List<AppointmentDto> getMyAppointments(Long patientId);

    List<AppointmentDto> getDoctorAppointments(Long doctorId);

    AppointmentDto updateStatus(Long appointmentId, AppointmentStatusUpdateRequest request);

    List<AppointmentDto> getAllAppointments();
}

