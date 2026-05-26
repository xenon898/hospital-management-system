package com.hospital.users.config;

import com.hospital.users.entity.AppUser;
import com.hospital.users.entity.Role;
import com.hospital.users.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    public AdminBootstrap(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled || appUserRepository.existsByUsername("admin")) {
            return;
        }

        AppUser admin = AppUser.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        appUserRepository.save(admin);
    }
}
