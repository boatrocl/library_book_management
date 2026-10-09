package com.libraflow.library.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.entity.UserProfile;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.request.RegisterRequest;
import com.libraflow.library.dto.response.AuthResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.security.JwtService;
import com.libraflow.library.service.AuthService;
import jakarta.persistence.EntityManager;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EntityManager entityManager; // ใช้ EntityManager บันทึกแทน Repository ย่อย

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EntityManager entityManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.entityManager = entityManager;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        // สร้าง User ด้วย Constructor ที่เราเพิ่งเพิ่ม
        User user = new User(
            request.username(),
            passwordEncoder.encode(request.password()),
            request.email(),
            UserRole.MEMBER,
            true,
            MemberTier.STUDENT.name() // แปลง Enum เป็น String ตามโครงสร้าง
        );

        User savedUser = userRepository.save(user);

        // สร้าง Profile โดยใช้ Constructor ที่เพื่อนคุณเตรียมไว้แล้ว
        UserProfile profile = new UserProfile(
            savedUser,
            request.firstName(),
            request.lastName(),
            request.phoneNumber(),
            request.address()
        );

        entityManager.persist(profile);

        String jwtToken = jwtService.generateToken(savedUser);

        return new AuthResponse(
                jwtToken,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                savedUser.getUsername(),
                savedUser.getRole().name()
        );
    }
}