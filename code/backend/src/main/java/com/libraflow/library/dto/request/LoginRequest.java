package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "กรุณากรอกชื่อผู้ใช้")
        @Size(
                max = 50,
                message = "ชื่อผู้ใช้ต้องไม่เกิน 50 ตัวอักษร"
        )
        String username,

        @NotBlank(message = "กรุณากรอกรหัสผ่าน")
        @Size(
                max = 100,
                message = "รหัสผ่านยาวเกินไป"
        )
        String password

) {
}