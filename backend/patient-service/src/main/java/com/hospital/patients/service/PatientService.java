package com.hospital.patients.service;

import com.hospital.patients.dto.PatientProfileDto;
import com.hospital.patients.dto.PrescriptionDto;

import java.util.List;

public interface PatientService {
    PatientProfileDto createPatient(PatientProfileDto dto);

    List<PatientProfileDto> getAllPatients();

    PatientProfileDto getMyProfile(Long userId);

    List<PrescriptionDto> getMyPrescriptions(Long userId, String authorization);
}

