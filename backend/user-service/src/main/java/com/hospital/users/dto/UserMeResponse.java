package com.hospital.users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMeResponse {

    private String userId;

    private String role;
}

