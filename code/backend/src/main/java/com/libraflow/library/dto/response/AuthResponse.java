package com.libraflow.library.dto.response;

public record AuthResponse(

        String token,

        String tokenType,

        long expiresInSeconds,

        String username,

        String role

) {
}