package com.hospital.patients.validation;

public final class PhoneNumberValidator {
    private static final String INDIAN_MOBILE_PATTERN = "^[6-9][0-9]{9}$";

    private PhoneNumberValidator() {
    }

    public static String normalize(String phone) {
        return phone == null ? null : phone.trim();
    }

    public static boolean isValid(String phone) {
        if (phone == null || phone.isBlank()) {
            return true;
        }
        String clean = normalize(phone);
        if (!clean.matches(INDIAN_MOBILE_PATTERN)) {
            return false;
        }
        if (clean.chars().distinct().count() == 1) {
            return false;
        }
        return !"1234567890".equals(clean);
    }

    public static String message() {
        return "Phone must be a unique 10 digit mobile number starting with 6, 7, 8, or 9";
    }
}
