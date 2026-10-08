package com.libraflow.library.dto.response;

public record UserManagementResponse(
        Long id,
        String username,
        String email,
        String role,
        String memberTier,
        boolean isActive
) {
}