package com.hospital.patients.validation;

import com.hospital.patients.dto.PatientProfileDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidPhoneNumbers() {
        assertFalse(PhoneNumberValidator.isValid("9999999999"));
        assertFalse(PhoneNumberValidator.isValid("1111111111"));
        assertFalse(PhoneNumberValidator.isValid("1234567890"));
        assertFalse(PhoneNumberValidator.isValid("5123456789"));
        assertTrue(PhoneNumberValidator.isValid("9876543211"));
    }

    @Test
    void rejectsInvalidAge() {
        PatientProfileDto zeroAge = PatientProfileDto.builder()
                .userId(1L)
                .name("Rahul Kumar")
                .age(0)
                .phone("9876543211")
                .build();
        PatientProfileDto tooOld = PatientProfileDto.builder()
                .userId(1L)
                .name("Rahul Kumar")
                .age(121)
                .phone("9876543211")
                .build();

        assertFalse(validator.validate(zeroAge).isEmpty());
        assertFalse(validator.validate(tooOld).isEmpty());
    }
}
