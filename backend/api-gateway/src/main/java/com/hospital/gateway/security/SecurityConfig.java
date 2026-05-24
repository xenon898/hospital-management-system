package com.hospital.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
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
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doctors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/doctors/me", "/api/doctors/patient-history/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/doctors/prescriptions").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/patients").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/patients").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/patients/me").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/my").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/doctor").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/*/status").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/appointments").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        http.addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}

