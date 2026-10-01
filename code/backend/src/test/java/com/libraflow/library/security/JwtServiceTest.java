package com.libraflow.library.security;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final long EXPIRATION_MS =
            3_600_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        byte[] secretBytes =
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8);

        String base64Secret =
                Base64.getEncoder()
                        .encodeToString(secretBytes);

        jwtService =
                new JwtService(
                        base64Secret,
                        EXPIRATION_MS
                );
    }

    @Test
    void generateToken_shouldContainUsernameAndRole() {

        User user =
                mock(User.class);

        when(user.getUsername())
                .thenReturn("admin");

        when(user.getRole())
                .thenReturn(UserRole.ADMIN);

        String token =
                jwtService.generateToken(user);

        assertFalse(token.isBlank());

        assertEquals(
                "admin",
                jwtService.extractUsername(token)
        );

        assertEquals(
                "ADMIN",
                jwtService.extractRole(token)
        );

        assertEquals(
                EXPIRATION_MS,
                jwtService.getExpirationMs()
        );
    }

    @Test
    void constructor_shouldRejectInvalidBase64Secret() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new JwtService(
                                        "%%%invalid-base64%%%",
                                        EXPIRATION_MS
                                )
                );

        assertEquals(
                "JWT_SECRET must be a valid Base64 string",
                exception.getMessage()
        );
    }

    @Test
    void constructor_shouldRejectSecretShorterThan256Bits() {

        String shortSecret =
                Base64.getEncoder()
                        .encodeToString(
                                "short-secret"
                                        .getBytes(
                                                StandardCharsets.UTF_8
                                        )
                        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new JwtService(
                                        shortSecret,
                                        EXPIRATION_MS
                                )
                );

        assertEquals(
                "JWT_SECRET must contain at least 32 bytes (256 bits)",
                exception.getMessage()
        );
    }

    @Test
    void constructor_shouldRejectNonPositiveExpiration() {

        byte[] secretBytes =
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8);

        String base64Secret =
                Base64.getEncoder()
                        .encodeToString(secretBytes);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new JwtService(
                                        base64Secret,
                                        0
                                )
                );

        assertEquals(
                "JWT_EXPIRATION must be greater than 0",
                exception.getMessage()
        );
    }
}