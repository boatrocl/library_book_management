package com.libraflow.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "กรุณาระบุชื่อผู้ใช้") String username,
        @NotBlank(message = "กรุณาระบุรหัสผ่าน") String password,
        @NotBlank(message = "กรุณาระบุอีเมล") @Email(message = "รูปแบบอีเมลไม่ถูกต้อง") String email,
        @NotBlank(message = "กรุณาระบุชื่อจริง") String firstName,
        @NotBlank(message = "กรุณาระบุนามสกุล") String lastName,
        String phoneNumber,
        String address
) {}