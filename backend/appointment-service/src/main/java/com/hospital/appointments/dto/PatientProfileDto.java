package com.hospital.appointments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientProfileDto {
    private Long id;
    private Long userId;
    private String name;
    private Integer age;
    private String phone;
}
