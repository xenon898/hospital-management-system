package com.hospital.appointments.config;

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
        jdbcTemplate.execute("ALTER TABLE appointments ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE appointments ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_appointments_patient_time ON appointments (patient_id, appointment_time)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_appointments_doctor_time ON appointments (doctor_id, appointment_time)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments (status)");
    }
}
