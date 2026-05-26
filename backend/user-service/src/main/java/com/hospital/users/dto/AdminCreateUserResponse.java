package com.hospital.users.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminCreateUserResponse {

    private Long userId;
    private Long profileId;
    private String message;
}
