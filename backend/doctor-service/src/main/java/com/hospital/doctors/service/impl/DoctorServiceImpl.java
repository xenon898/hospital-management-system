package com.hospital.doctors.service.impl;

import com.hospital.doctors.dto.AppointmentDto;
import com.hospital.doctors.dto.DoctorProfileDto;
import com.hospital.doctors.dto.PrescriptionCreateRequest;
import com.hospital.doctors.dto.PrescriptionDto;
import com.hospital.doctors.entity.DoctorProfile;
import com.hospital.doctors.entity.Prescription;
import com.hospital.doctors.repository.DoctorProfileRepository;
import com.hospital.doctors.repository.PrescriptionRepository;
import com.hospital.doctors.service.DoctorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final RestTemplate restTemplate;

    @Value("${services.appointment.url}")
    private String appointmentUrl;

    public DoctorServiceImpl(DoctorProfileRepository doctorRepo,
                             PrescriptionRepository prescriptionRepo,
                             RestTemplate restTemplate) {
        this.doctorRepo = doctorRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.restTemplate = restTemplate;
    }

    @Override
    public DoctorProfileDto createDoctor(DoctorProfileDto dto) {
        if (doctorRepo.existsByUserId(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor profile already exists for this user");
        }
        DoctorProfile profile = DoctorProfile.builder()
                .userId(dto.getUserId())
                .name(dto.getName().trim())
                .specialization(dto.getSpecialization().trim())
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
    public PrescriptionDto addPrescription(PrescriptionCreateRequest request, Long doctorUserId, String authorization) {
        AppointmentDto appointment = loadOwnedAppointment(request.getAppointmentId(), authorization);
        if (!doctorUserId.equals(appointment.getDoctorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment is not assigned to this doctor");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prescription can be created only after completing the appointment");
        }
        if (request.getPatientId() != null && !request.getPatientId().equals(appointment.getPatientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient does not match the selected appointment");
        }
        if (prescriptionRepo.existsByAppointmentId(request.getAppointmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prescription already exists for this appointment");
        }

        Prescription prescription = Prescription.builder()
                .appointmentId(request.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(doctorUserId)
                .content(request.getContent().trim())
                .build();

        Prescription saved = prescriptionRepo.save(prescription);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientHistory(Long patientId, Long doctorUserId) {
        return prescriptionRepo.findByPatientIdAndDoctorIdOrderByCreatedAtDesc(patientId, doctorUserId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions(Long patientUserId) {
        return prescriptionRepo.findByPatientIdOrderByCreatedAtDesc(patientUserId)
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

    private AppointmentDto loadOwnedAppointment(Long appointmentId, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<AppointmentDto> response = restTemplate.exchange(
                    appointmentUrl + "/api/appointments/" + appointmentId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AppointmentDto.class
            );
            if (response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment lookup returned no data");
            }
            return response.getBody();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to verify appointment ownership");
        }
    }
}

