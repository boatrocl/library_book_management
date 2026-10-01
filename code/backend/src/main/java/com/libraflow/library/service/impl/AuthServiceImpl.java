package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.response.AuthResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.security.JwtService;
import com.libraflow.library.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;
    }

    @Override
    public AuthResponse login(
            LoginRequest request
    ) {

        String username =
                request.username()
                        .trim();

        User user =
                userRepository
                        .findByUsername(
                                username
                        )
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.AUTHENTICATION_FAILED
                                        )
                        );

        if (
                !user.isActive()
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                )
        ) {

            throw new BusinessException(
                    ErrorCode.AUTHENTICATION_FAILED
            );
        }

        String token =
                jwtService.generateToken(
                        user
                );

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs()
                        / 1000,
                user.getUsername(),
                user.getRole().name()
        );
    }
}