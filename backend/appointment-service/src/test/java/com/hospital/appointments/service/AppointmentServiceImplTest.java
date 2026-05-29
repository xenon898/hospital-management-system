package com.hospital.appointments.service;

import com.hospital.appointments.dto.AppointmentCreateRequest;
import com.hospital.appointments.dto.AppointmentStatusUpdateRequest;
import com.hospital.appointments.dto.AppointmentUpdateRequest;
import com.hospital.appointments.entity.Appointment;
import com.hospital.appointments.entity.AppointmentStatus;
import com.hospital.appointments.repository.AppointmentRepository;
import com.hospital.appointments.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    private final AppointmentRepository repository = mock(AppointmentRepository.class);
    private final AppointmentServiceImpl service = new AppointmentServiceImpl(repository, new RestTemplate());

    AppointmentServiceImplTest() {
        ReflectionTestUtils.setField(service, "doctorUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(service, "patientUrl", "http://localhost:8083");
    }

    @Test
    void rejectsPastAppointmentBooking() {
        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setDoctorId(5L);
        request.setAppointmentTime(LocalDateTime.now().minusDays(1));

        assertThrows(ResponseStatusException.class, () -> service.book(request, 10L, "Bearer token"));
    }

    @Test
    void rejectsDuplicateDoctorSlot() {
        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setDoctorId(5L);
        request.setAppointmentTime(LocalDateTime.now().plusDays(1));
        when(repository.existsByDoctorIdAndAppointmentTimeBetweenAndStatusNot(
                org.mockito.Mockito.eq(5L),
                org.mockito.Mockito.any(),
                org.mockito.Mockito.any(),
                org.mockito.Mockito.eq(AppointmentStatus.CANCELLED)
        )).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.book(request, 10L, "Bearer token"));
    }

    @Test
    void rejectsPatientEditingAnotherPatientsAppointment() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .doctorId(5L)
                .patientId(99L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.PENDING)
                .build();
        AppointmentUpdateRequest request = new AppointmentUpdateRequest();
        request.setDoctorId(5L);
        request.setAppointmentTime(LocalDateTime.now().plusDays(2));
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class, () -> service.reschedule(1L, request, 10L, "Bearer token"));
    }

    @Test
    void rejectsWrongDoctorStatusUpdate() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .doctorId(5L)
                .patientId(10L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.PENDING)
                .build();
        AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
        request.setStatus(AppointmentStatus.CONFIRMED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class, () -> service.updateStatus(1L, request, 6L, "Bearer token"));
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .doctorId(5L)
                .patientId(10L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.COMPLETED)
                .build();
        AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
        request.setStatus(AppointmentStatus.CANCELLED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class, () -> service.updateStatus(1L, request, 5L, "Bearer token"));
    }
}
