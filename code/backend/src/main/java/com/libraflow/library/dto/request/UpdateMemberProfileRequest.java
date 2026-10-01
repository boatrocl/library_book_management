package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberProfileRequest(
        @NotBlank(message = "กรุณากรอกชื่อจริง")
        @Size(max = 100, message = "ชื่อจริงยาวเกินไป")
        String firstName,

        @NotBlank(message = "กรุณากรอกนามสกุล")
        @Size(max = 100, message = "นามสกุลยาวเกินไป")
        String lastName,

        @Size(max = 20, message = "เบอร์โทรศัพท์ยาวเกินไป")
        String phoneNumber,

        String address
) {}