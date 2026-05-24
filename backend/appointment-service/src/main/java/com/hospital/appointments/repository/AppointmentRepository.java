package com.hospital.appointments.repository;

import com.hospital.appointments.entity.Appointment;
import com.hospital.appointments.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(Long doctorId);

    List<Appointment> findAllByOrderByAppointmentTimeDesc();
}

