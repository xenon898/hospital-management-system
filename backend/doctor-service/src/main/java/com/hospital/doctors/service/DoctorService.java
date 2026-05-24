package com.hospital.doctors.service;

import com.hospital.doctors.dto.DoctorProfileDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.dto.PrescriptionDto;

import java.util.List;

public interface DoctorService {

    DoctorProfileDto createDoctor(DoctorProfileDto dto);

    List<DoctorProfileDto> getAllDoctors();

    DoctorProfileDto getMyProfile(Long userId);

    PrescriptionDto addPrescription(PrescriptionCreateRequest request, Long doctorId);

    List<PrescriptionDto> getPatientHistory(Long patientId);
}

