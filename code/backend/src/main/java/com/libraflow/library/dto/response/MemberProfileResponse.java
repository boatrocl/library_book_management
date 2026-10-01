package com.libraflow.library.dto.response;

public record MemberProfileResponse(
        Long id,
        String username,
        String email,
        String role,
        String memberTier,
        String firstName,
        String lastName,
        String phoneNumber,
        String address
) {}