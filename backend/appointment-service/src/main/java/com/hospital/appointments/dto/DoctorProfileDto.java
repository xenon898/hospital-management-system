package com.hospital.appointments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorProfileDto {
    private Long id;
    private Long userId;
    private String name;
    private String specialization;
}
