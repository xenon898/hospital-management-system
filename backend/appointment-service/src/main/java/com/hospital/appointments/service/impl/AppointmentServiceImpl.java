package com.hospital.appointments.service.impl;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.entity.Appointment;
import com.hospital.appointments.entity.AppointmentStatus;
import com.hospital.appointments.repository.AppointmentRepository;
import com.hospital.appointments.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    @Override
    public AppointmentDto book(AppointmentCreateRequest request, Long patientId) {
        Appointment appointment = Appointment.builder()
                .doctorId(request.getDoctorId())
                .patientId(patientId)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepo.save(appointment);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointments(Long patientId) {
        return appointmentRepo.findByPatientIdOrderByAppointmentTimeDesc(patientId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorId) {
        return appointmentRepo.findByDoctorIdOrderByAppointmentTimeDesc(doctorId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public AppointmentDto updateStatus(Long appointmentId, AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found id=" + appointmentId));

        appointment.setStatus(request.getStatus());
        return toDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepo.findAllByOrderByAppointmentTimeDesc().stream().map(this::toDto).toList();
    }

    private AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .doctorId(a.getDoctorId())
                .patientId(a.getPatientId())
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus())
                .build();
    }
}

