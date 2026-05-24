package com.hospital.users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private String role;

    /** user identifier (stored in token subject as string). */
    private String userId;
}

