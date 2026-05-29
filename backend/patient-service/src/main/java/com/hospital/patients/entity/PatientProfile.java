package com.hospital.patients.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "patient_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_user", columnNames = "userId"),
                @UniqueConstraint(name = "uk_patient_phone", columnNames = "phone")
        },
        indexes = {
                @Index(name = "idx_patient_user_id", columnList = "userId"),
                @Index(name = "idx_patient_phone", columnList = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References userId stored in User Service (no FK for simplicity). */
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

