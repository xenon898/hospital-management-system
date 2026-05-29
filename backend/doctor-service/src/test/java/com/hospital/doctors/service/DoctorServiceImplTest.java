package com.hospital.doctors.service;

import com.hospital.doctors.dto.AppointmentDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.repository.DoctorProfileRepository;
import com.hospital.doctors.repository.PrescriptionRepository;
import com.hospital.doctors.service.impl.DoctorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DoctorServiceImplTest {
    private final DoctorProfileRepository doctorRepository = mock(DoctorProfileRepository.class);
    private final PrescriptionRepository prescriptionRepository = mock(PrescriptionRepository.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final DoctorServiceImpl service = new DoctorServiceImpl(doctorRepository, prescriptionRepository, restTemplate);

    DoctorServiceImplTest() {
        ReflectionTestUtils.setField(service, "appointmentUrl", "http://localhost:8084");
    }

    @Test
    void rejectsPrescriptionForWrongDoctorAppointment() {
        AppointmentDto appointment = new AppointmentDto();
        appointment.setId(1L);
        appointment.setDoctorId(99L);
        appointment.setPatientId(10L);
        appointment.setStatus("COMPLETED");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AppointmentDto.class)))
                .thenReturn(ResponseEntity.ok(appointment));

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setAppointmentId(1L);
        request.setContent("Take medicine after food");

        assertThrows(ResponseStatusException.class, () -> service.addPrescription(request, 5L, "Bearer token"));
    }

    @Test
    void rejectsPrescriptionBeforeAppointmentCompletion() {
        AppointmentDto appointment = new AppointmentDto();
        appointment.setId(1L);
        appointment.setDoctorId(5L);
        appointment.setPatientId(10L);
        appointment.setStatus("CONFIRMED");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AppointmentDto.class)))
                .thenReturn(ResponseEntity.ok(appointment));

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setAppointmentId(1L);
        request.setContent("Take medicine after food");

        assertThrows(ResponseStatusException.class, () -> service.addPrescription(request, 5L, "Bearer token"));
    }

    @Test
    void rejectsDuplicatePrescriptionForAppointment() {
        AppointmentDto appointment = new AppointmentDto();
        appointment.setId(1L);
        appointment.setDoctorId(5L);
        appointment.setPatientId(10L);
        appointment.setStatus("COMPLETED");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AppointmentDto.class)))
                .thenReturn(ResponseEntity.ok(appointment));
        when(prescriptionRepository.existsByAppointmentId(1L)).thenReturn(true);

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setAppointmentId(1L);
        request.setContent("Take medicine after food");

        assertThrows(ResponseStatusException.class, () -> service.addPrescription(request, 5L, "Bearer token"));
    }
}
