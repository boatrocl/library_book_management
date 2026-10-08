package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.response.AuthResponse;
import com.libraflow.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.libraflow.library.dto.request.RegisterRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "เข้าสู่ระบบและรับ JWT"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @Operation(
            summary = "เข้าสู่ระบบ",
            description =
                    "ตรวจ username/password และคืน Bearer JWT"
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "เข้าสู่ระบบสำเร็จ"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description =
                            "request ไม่ผ่าน validation"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description =
                            "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"
            )
    })

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(
                        request
                )
        );
    }

    @Operation(summary = "สมัครสมาชิกใหม่", description = "สร้างบัญชีผู้ใช้ใหม่พร้อมโปรไฟล์")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}