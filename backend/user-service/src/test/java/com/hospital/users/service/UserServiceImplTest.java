package com.hospital.users.service;

import com.hospital.users.config.JwtProperties;
import com.hospital.users.dto.LoginRequest;
import com.hospital.users.entity.AppUser;
import com.hospital.users.entity.Role;
import com.hospital.users.repository.AppUserRepository;
import com.hospital.users.security.JwtUtil;
import com.hospital.users.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final JwtProperties jwtProperties = new JwtProperties();
    private final UserServiceImpl service;

    UserServiceImplTest() {
        jwtProperties.setSecret("dev-secret-change-me-use-at-least-32-bytes");
        jwtProperties.setExpirationMs(604800000);
        service = new UserServiceImpl(repository, new JwtUtil(jwtProperties), new RestTemplate());
    }

    @Test
    void validLoginReturnsAdminToken() {
        AppUser user = AppUser.builder()
                .id(1L)
                .username("admin")
                .passwordHash(new BCryptPasswordEncoder().encode("admin123"))
                .role(Role.ADMIN)
                .build();
        when(repository.findByUsername("admin")).thenReturn(Optional.of(user));

        var response = service.login(LoginRequest.builder().username("admin").password("admin123").build());

        assertEquals("ADMIN", response.getRole());
        assertNotNull(response.getToken());
    }

    @Test
    void invalidLoginFails() {
        when(repository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.login(LoginRequest.builder().username("admin").password("wrong").build()));
    }
}
