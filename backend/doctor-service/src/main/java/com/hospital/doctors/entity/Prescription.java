package com.hospital.doctors.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "prescriptions",
        uniqueConstraints = @UniqueConstraint(name = "uk_prescription_appointment", columnNames = "appointmentId"),
        indexes = {
                @Index(name = "idx_prescriptions_patient", columnList = "patientId"),
                @Index(name = "idx_prescriptions_doctor", columnList = "doctorId"),
                @Index(name = "idx_prescriptions_appointment", columnList = "appointmentId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Kept as user ids to avoid complex cross-service foreign keys. */
    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false, length = 2000)
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

