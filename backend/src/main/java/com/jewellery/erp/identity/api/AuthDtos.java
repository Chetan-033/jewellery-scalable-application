package com.jewellery.erp.identity.api;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMinutes,
            UserInfo user
    ) {
    }

    public record UserInfo(
            String id,
            String username,
            String displayName,
            List<String> roles,
            List<String> permissions
    ) {
    }
}
