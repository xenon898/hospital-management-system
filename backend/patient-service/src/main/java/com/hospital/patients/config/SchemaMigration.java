package com.hospital.patients.config;

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
        jdbcTemplate.execute("ALTER TABLE patient_profiles ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE patient_profiles ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_user_id ON patient_profiles (user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_phone ON patient_profiles (phone)");
    }
}
