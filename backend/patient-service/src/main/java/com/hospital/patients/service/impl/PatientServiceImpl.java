package com.hospital.patients.service.impl;

import com.hospital.patients.dto.PatientProfileDto;
import com.hospital.patients.entity.PatientProfile;
import com.hospital.patients.repository.PatientProfileRepository;
import com.hospital.patients.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientProfileRepository patientRepo;

    public PatientServiceImpl(PatientProfileRepository patientRepo) {
        this.patientRepo = patientRepo;
    }

    @Override
    public PatientProfileDto createPatient(PatientProfileDto dto) {
        PatientProfile profile = PatientProfile.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .age(dto.getAge())
                .phone(dto.getPhone())
                .build();

        PatientProfile saved = patientRepo.save(profile);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileDto> getAllPatients() {
        return patientRepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileDto getMyProfile(Long userId) {
        PatientProfile profile = patientRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Patient profile not found for userId=" + userId));
        return toDto(profile);
    }

    private PatientProfileDto toDto(PatientProfile p) {
        return PatientProfileDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .name(p.getName())
                .age(p.getAge())
                .phone(p.getPhone())
                .build();
    }
}

