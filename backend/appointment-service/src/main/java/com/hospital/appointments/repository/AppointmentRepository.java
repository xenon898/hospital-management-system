package com.hospital.appointments.repository;

import com.hospital.appointments.entity.Appointment;
import com.hospital.appointments.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(Long doctorId);

    List<Appointment> findAllByOrderByAppointmentTimeDesc();

    boolean existsByDoctorIdAndAppointmentTimeBetweenAndStatusNot(
            Long doctorId,
            LocalDateTime from,
            LocalDateTime to,
            AppointmentStatus status
    );

    boolean existsByDoctorIdAndAppointmentTimeBetweenAndStatusNotAndIdNot(
            Long doctorId,
            LocalDateTime from,
            LocalDateTime to,
            AppointmentStatus status,
            Long id
    );

    boolean existsByPatientIdAndDoctorIdAndAppointmentTimeAndStatusNot(
            Long patientId,
            Long doctorId,
            LocalDateTime appointmentTime,
            AppointmentStatus status
    );

    boolean existsByPatientIdAndDoctorIdAndAppointmentTimeAndStatusNotAndIdNot(
            Long patientId,
            Long doctorId,
            LocalDateTime appointmentTime,
            AppointmentStatus status,
            Long id
    );
}

