package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(

        @NotBlank(message = "กรุณาระบุสถานะผู้ใช้")
        @Pattern(
                regexp = "ACTIVE|SUSPENDED",
                message = "สถานะต้องเป็น ACTIVE หรือ SUSPENDED"
        )
        String status

) {
}