package com.hospital.doctors.controller;

import com.hospital.doctors.dto.DoctorProfileDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.dto.PrescriptionDto;
import com.hospital.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
                                           @RequestHeader("Authorization") String authorization,
                                           Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        doctorService.getMyProfile(userId);
        return doctorService.addPrescription(request, userId, authorization);
    }

    /** DOCTOR: view patient prescription history. */
    @GetMapping("/patient-history/{patientId}")
    public List<PrescriptionDto> getPatientHistory(@PathVariable("patientId") Long patientId,
                                                   Authentication authentication) {
        Long doctorUserId = Long.valueOf(authentication.getName());
        return doctorService.getPatientHistory(patientId, doctorUserId);
    }

    /** PATIENT SERVICE: fetch prescriptions for the logged-in patient user only. */
    @GetMapping("/internal/patient-prescriptions/{patientId}")
    public List<PrescriptionDto> getPatientPrescriptions(@PathVariable("patientId") Long patientId,
                                                         Authentication authentication) {
        Long authenticatedPatientId = Long.valueOf(authentication.getName());
        if (!authenticatedPatientId.equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patients can only access their own prescriptions");
        }
        return doctorService.getPatientPrescriptions(patientId);
    }
}

