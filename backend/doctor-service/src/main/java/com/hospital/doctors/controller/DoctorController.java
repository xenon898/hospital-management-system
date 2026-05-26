package com.hospital.doctors.controller;

import com.hospital.doctors.dto.DoctorProfileDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.dto.PrescriptionDto;
import com.hospital.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /** ADMIN: create doctor profile for a userId. */
    @PostMapping
    public DoctorProfileDto createDoctor(@RequestBody @Valid DoctorProfileDto dto) {
        return doctorService.createDoctor(dto);
    }

    /** Anyone with auth: list doctors. */
    @GetMapping
    public List<DoctorProfileDto> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    /** Returns doctor profile for the currently logged-in DOCTOR user. */
    @GetMapping("/me")
    public DoctorProfileDto getMyProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return doctorService.getMyProfile(userId);
    }

    /** DOCTOR: add prescription for patient. */
    @PostMapping("/prescriptions")
    public PrescriptionDto addPrescription(@RequestBody @Valid PrescriptionCreateRequest request,
                                           Authentication authentication) {
        // beginner note: treat doctorId as doctorProfile.id; here we simplify by using doctorId from profile lookup.
        Long userId = Long.valueOf(authentication.getName());
        Long doctorId = doctorService.getMyProfile(userId).getId();
        return doctorService.addPrescription(request, doctorId);
    }

    /** DOCTOR: view patient prescription history. */
    @GetMapping("/patient-history/{patientId}")
    public List<PrescriptionDto> getPatientHistory(@PathVariable("patientId") Long patientId) {
        return doctorService.getPatientHistory(patientId);
    }

    /** PATIENT SERVICE: fetch prescriptions after Patient Service verifies the logged-in patient profile. */
    @GetMapping("/internal/patient-prescriptions/{patientId}")
    public List<PrescriptionDto> getPatientPrescriptions(@PathVariable("patientId") Long patientId) {
        return doctorService.getPatientHistory(patientId);
    }
}

