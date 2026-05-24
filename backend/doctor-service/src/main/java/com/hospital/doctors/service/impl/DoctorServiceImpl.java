package com.hospital.doctors.service.impl;

import com.hospital.doctors.dto.DoctorProfileDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.dto.PrescriptionDto;
import com.hospital.doctors.entity.DoctorProfile;
import com.hospital.doctors.entity.Prescription;
import com.hospital.doctors.repository.DoctorProfileRepository;
import com.hospital.doctors.repository.PrescriptionRepository;
import com.hospital.doctors.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorRepo;
    private final PrescriptionRepository prescriptionRepo;

    public DoctorServiceImpl(DoctorProfileRepository doctorRepo, PrescriptionRepository prescriptionRepo) {
        this.doctorRepo = doctorRepo;
        this.prescriptionRepo = prescriptionRepo;
    }

    @Override
    public DoctorProfileDto createDoctor(DoctorProfileDto dto) {
        DoctorProfile profile = DoctorProfile.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .specialization(dto.getSpecialization())
                .build();

        DoctorProfile saved = doctorRepo.save(profile);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileDto> getAllDoctors() {
        return doctorRepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getMyProfile(Long userId) {
        DoctorProfile profile = doctorRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found for userId=" + userId));
        return toDto(profile);
    }

    @Override
    public PrescriptionDto addPrescription(PrescriptionCreateRequest request, Long doctorId) {
        Prescription prescription = Prescription.builder()
                .appointmentId(request.getAppointmentId())
                .patientId(request.getPatientId())
                .doctorId(doctorId)
                .content(request.getContent())
                .build();

        Prescription saved = prescriptionRepo.save(prescription);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientHistory(Long patientId) {
        return prescriptionRepo.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toDto).toList();
    }

    private DoctorProfileDto toDto(DoctorProfile p) {
        return DoctorProfileDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .name(p.getName())
                .specialization(p.getSpecialization())
                .build();
    }

    private PrescriptionDto toDto(Prescription p) {
        return PrescriptionDto.builder()
                .id(p.getId())
                .appointmentId(p.getAppointmentId())
                .patientId(p.getPatientId())
                .doctorId(p.getDoctorId())
                .content(p.getContent())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

