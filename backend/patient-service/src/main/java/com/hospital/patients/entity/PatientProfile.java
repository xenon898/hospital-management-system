package com.hospital.patients.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_profiles")
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
}

