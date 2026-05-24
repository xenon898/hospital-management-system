package com.hospital.appointments.security;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/my").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/doctor").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/*/status").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/appointments").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
