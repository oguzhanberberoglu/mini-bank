package com.main.mini_bank.model.dto.response;

import java.time.Instant;

public class AuthResponse {

    private final String token;
    private final String tokenType;
    private final Instant expiresAt;

    public AuthResponse(String token, String tokenType, Instant expiresAt) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}