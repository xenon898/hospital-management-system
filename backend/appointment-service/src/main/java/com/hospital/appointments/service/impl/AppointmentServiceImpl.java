package com.hospital.appointments.service.impl;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentDto;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.dto.AppointmentUpdateRequest;
import com.hospital.appointments.dto.DoctorProfileDto;
import com.hospital.appointments.dto.PatientProfileDto;
import com.hospital.appointments.entity.Appointment;
import com.hospital.appointments.entity.AppointmentStatus;
import com.hospital.appointments.repository.AppointmentRepository;
import com.hospital.appointments.service.AppointmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final RestTemplate restTemplate;

    @Value("${services.doctor.url}")
    private String doctorUrl;

    @Value("${services.patient.url}")
    private String patientUrl;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepo, RestTemplate restTemplate) {
        this.appointmentRepo = appointmentRepo;
        this.restTemplate = restTemplate;
    }

    @Override
    public AppointmentDto book(AppointmentCreateRequest request, Long patientId, String authorization) {
        validateBookableSlot(patientId, request.getDoctorId(), request.getAppointmentTime(), null);

        Appointment appointment = Appointment.builder()
                .doctorId(request.getDoctorId())
                .patientId(patientId)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepo.save(appointment);
        return toDto(saved, authorization);
    }

    @Override
    public AppointmentDto reschedule(Long appointmentId, AppointmentUpdateRequest request, Long patientId, String authorization) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own appointments");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending appointments can be edited");
        }

        validateBookableSlot(patientId, request.getDoctorId(), request.getAppointmentTime(), appointmentId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentTime(request.getAppointmentTime());
        return toDto(appointment, authorization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointments(Long patientId, String authorization) {
        return toDtos(appointmentRepo.findByPatientIdOrderByAppointmentTimeDesc(patientId), authorization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorId, String authorization) {
        return toDtos(appointmentRepo.findByDoctorIdOrderByAppointmentTimeDesc(doctorId), authorization);
    }

    @Override
    public AppointmentDto updateStatus(Long appointmentId, AppointmentStatusUpdateRequest request, Long doctorId, String authorization) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctors can only update assigned appointments");
        }
        validateStatusTransition(appointment.getStatus(), request.getStatus());
        appointment.setStatus(request.getStatus());
        return toDto(appointment, authorization);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto getOwnedAppointment(Long appointmentId, Long userId, String role, String authorization) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if ("PATIENT".equals(role) && !appointment.getPatientId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to this patient");
        }
        if ("DOCTOR".equals(role) && !appointment.getDoctorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment is not assigned to this doctor");
        }
        return toDto(appointment, authorization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments(String authorization) {
        return toDtos(appointmentRepo.findAllByOrderByAppointmentTimeDesc(), authorization);
    }

    private List<AppointmentDto> toDtos(List<Appointment> appointments, String authorization) {
        Map<Long, String> doctorNames = loadDoctorNames(authorization);
        return appointments.stream()
                .map(a -> toDto(a, doctorNames.get(a.getDoctorId()), loadPatientName(a.getPatientId(), authorization).orElse(null)))
                .toList();
    }

    private AppointmentDto toDto(Appointment a, String authorization) {
        String doctorName = loadDoctorNames(authorization).get(a.getDoctorId());
        String patientName = loadPatientName(a.getPatientId(), authorization).orElse(null);
        return toDto(a, doctorName, patientName);
    }

    private AppointmentDto toDto(Appointment a, String doctorName, String patientName) {
        return AppointmentDto.builder()
                .id(a.getId())
                .doctorId(a.getDoctorId())
                .doctorName(doctorName)
                .patientId(a.getPatientId())
                .patientName(patientName)
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus())
                .build();
    }

    private void validateBookableSlot(Long patientId, Long doctorId, LocalDateTime appointmentTime, Long currentId) {
        if (doctorId == null || doctorId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid doctorId is required");
        }
        if (appointmentTime == null || !appointmentTime.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment time must be in the future");
        }

        LocalDateTime slotStart = appointmentTime.minusMinutes(29);
        LocalDateTime slotEnd = appointmentTime.plusMinutes(29);
        boolean doctorBusy = currentId == null
                ? appointmentRepo.existsByDoctorIdAndAppointmentTimeBetweenAndStatusNot(
                        doctorId, slotStart, slotEnd, AppointmentStatus.CANCELLED)
                : appointmentRepo.existsByDoctorIdAndAppointmentTimeBetweenAndStatusNotAndIdNot(
                        doctorId, slotStart, slotEnd, AppointmentStatus.CANCELLED, currentId);
        if (doctorBusy) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor is not available for this time slot");
        }

        boolean duplicate = currentId == null
                ? appointmentRepo.existsByPatientIdAndDoctorIdAndAppointmentTimeAndStatusNot(
                        patientId, doctorId, appointmentTime, AppointmentStatus.CANCELLED)
                : appointmentRepo.existsByPatientIdAndDoctorIdAndAppointmentTimeAndStatusNotAndIdNot(
                        patientId, doctorId, appointmentTime, AppointmentStatus.CANCELLED, currentId);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate appointment for this doctor and time");
        }
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        if (current == next) {
            return;
        }
        boolean valid = switch (current) {
            case PENDING -> next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.CANCELLED;
            case CONFIRMED -> next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status transition from " + current + " to " + next);
        }
    }

    private Map<Long, String> loadDoctorNames(String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<List<DoctorProfileDto>> response = restTemplate.exchange(
                    doctorUrl + "/api/doctors",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            List<DoctorProfileDto> doctors = response.getBody() == null ? List.of() : response.getBody();
            return doctors.stream()
                    .filter(d -> d.getUserId() != null && d.getName() != null)
                    .collect(Collectors.toMap(DoctorProfileDto::getUserId, DoctorProfileDto::getName, (left, right) -> left));
        } catch (RestClientException ex) {
            return Map.of();
        }
    }

    private Optional<String> loadPatientName(Long patientUserId, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<PatientProfileDto> response = restTemplate.exchange(
                    patientUrl + "/api/patients/internal/by-user/" + patientUserId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    PatientProfileDto.class
            );
            return Optional.ofNullable(response.getBody()).map(PatientProfileDto::getName).filter(Objects::nonNull);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }
}

