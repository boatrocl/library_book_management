package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.response.AuthResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private User user;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, entityManager);
    }

    @Test
    void login_shouldReturnJwtWhenCredentialsAreCorrect() {

        LoginRequest request =
                new LoginRequest(
                        " admin ",
                        "Admin@123"
                );

        when(
                userRepository.findByUsername(
                        "admin"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(user.isActive())
                .thenReturn(true);

        when(user.getPasswordHash())
                .thenReturn("stored-bcrypt-hash");

        when(
                passwordEncoder.matches(
                        "Admin@123",
                        "stored-bcrypt-hash"
                )
        ).thenReturn(true);

        when(
                jwtService.generateToken(user)
        ).thenReturn("test-jwt-token");

        when(
                jwtService.getExpirationMs()
        ).thenReturn(86_400_000L);

        when(user.getUsername())
                .thenReturn("admin");

        when(user.getRole())
                .thenReturn(UserRole.ADMIN);

        AuthResponse response =
                authService.login(request);

        assertEquals(
                "test-jwt-token",
                response.token()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                86_400L,
                response.expiresInSeconds()
        );

        assertEquals(
                "admin",
                response.username()
        );

        assertEquals(
                "ADMIN",
                response.role()
        );

        verify(userRepository)
                .findByUsername("admin");

        verify(passwordEncoder)
                .matches(
                        "Admin@123",
                        "stored-bcrypt-hash"
                );

        verify(jwtService)
                .generateToken(user);
    }

    @Test
    void login_shouldFailWhenUserDoesNotExist() {

        LoginRequest request =
                new LoginRequest(
                        "unknown",
                        "password"
                );

        when(
                userRepository.findByUsername(
                        "unknown"
                )
        ).thenReturn(
                Optional.empty()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                authService.login(
                                        request
                                )
                );

        assertEquals(
                ErrorCode.AUTHENTICATION_FAILED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void login_shouldFailWhenPasswordIsIncorrect() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "wrongpassword"
                );

        when(
                userRepository.findByUsername(
                        "admin"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(user.isActive())
                .thenReturn(true);

        when(user.getPasswordHash())
                .thenReturn("stored-bcrypt-hash");

        when(
                passwordEncoder.matches(
                        "wrongpassword",
                        "stored-bcrypt-hash"
                )
        ).thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                authService.login(
                                        request
                                )
                );

        assertEquals(
                ErrorCode.AUTHENTICATION_FAILED,
                exception.getErrorCode()
        );

        verify(
                jwtService,
                never()
        ).generateToken(user);
    }

    @Test
    void login_shouldFailWhenUserIsInactive() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "Admin@123"
                );

        when(
                userRepository.findByUsername(
                        "admin"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(user.isActive())
                .thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                authService.login(
                                        request
                                )
                );

        assertEquals(
                ErrorCode.AUTHENTICATION_FAILED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }
}