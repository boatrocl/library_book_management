package com.libraflow.library.controller.api;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // สร้าง DTO แบบ Record เพื่อคัดลอกเฉพาะข้อมูลที่ปลอดภัย ป้องกัน Circular Reference
    public record UserDto(Long id, String username, String email, Object role, Boolean isActive, Object memberTier) {}

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // ดึงข้อมูล User และแปลงเป็น UserDto ก่อนส่งกลับไปให้ Frontend
        List<UserDto> users = userRepository.findAll().stream()
                .map(u -> new UserDto(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole(),
                        u.isActive(),      // หมายเหตุ: หาก Entity ของคุณตั้งชื่อว่า isActive() ให้เปลี่ยนเป็น u.isActive()
                        u.getMemberTier()
                ))
                .toList();
        
        return ResponseEntity.ok(users);
    }
}