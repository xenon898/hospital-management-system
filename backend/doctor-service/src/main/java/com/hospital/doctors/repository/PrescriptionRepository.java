package com.hospital.doctors.repository;

import com.hospital.doctors.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    boolean existsByAppointmentId(Long appointmentId);

    List<Prescription> findByPatientIdAndDoctorIdOrderByCreatedAtDesc(Long patientId, Long doctorId);
}

