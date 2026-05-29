package com.hospital.doctors.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigration {
    private final JdbcTemplate jdbcTemplate;

    public SchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        jdbcTemplate.execute("ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_doctor_user_id ON doctor_profiles (user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_prescriptions_appointment ON prescriptions (appointment_id)");
    }
}
