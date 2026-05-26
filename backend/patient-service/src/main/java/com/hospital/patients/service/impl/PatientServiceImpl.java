package com.hospital.patients.service.impl;

import com.hospital.patients.dto.PatientProfileDto;
import com.hospital.patients.dto.PrescriptionDto;
import com.hospital.patients.entity.PatientProfile;
import com.hospital.patients.repository.PatientProfileRepository;
import com.hospital.patients.service.PatientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientProfileRepository patientRepo;
    private final RestTemplate restTemplate;

    @Value("${services.doctor.url}")
    private String doctorUrl;

    public PatientServiceImpl(PatientProfileRepository patientRepo, RestTemplate restTemplate) {
        this.patientRepo = patientRepo;
        this.restTemplate = restTemplate;
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

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getMyPrescriptions(Long userId, String authorization) {
        PatientProfile profile = patientRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Patient profile not found for userId=" + userId));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<List<PrescriptionDto>> response = restTemplate.exchange(
                    doctorUrl + "/api/doctors/internal/patient-prescriptions/" + profile.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Unable to load prescriptions: " + ex.getMessage());
        }
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

