package com.hospital.patients.controller;

import com.hospital.patients.dto.PatientProfileDto;
import com.hospital.patients.dto.PrescriptionDto;
import com.hospital.patients.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** ADMIN: create patient profile. */
    @PostMapping
    public PatientProfileDto createPatient(@RequestBody @Valid PatientProfileDto dto) {
        return patientService.createPatient(dto);
    }

    /** List patients. */
    @GetMapping
    public List<PatientProfileDto> getAllPatients() {
        return patientService.getAllPatients();
    }

    /** PATIENT: get my profile based on token principal. */
    @GetMapping("/me")
    public PatientProfileDto getMyProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return patientService.getMyProfile(userId);
    }

    /** PATIENT: prescriptions written for the logged-in patient's profile. */
    @GetMapping("/my-prescriptions")
    public List<PrescriptionDto> getMyPrescriptions(Authentication authentication,
                                                    @RequestHeader("Authorization") String authorization) {
        Long userId = Long.valueOf(authentication.getName());
        return patientService.getMyPrescriptions(userId, authorization);
    }
}

