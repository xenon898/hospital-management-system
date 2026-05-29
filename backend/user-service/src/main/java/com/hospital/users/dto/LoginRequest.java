package com.hospital.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 60, message = "username must be 60 characters or fewer")
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 72, message = "password must be 72 characters or fewer")
    private String password;
}

