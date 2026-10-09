package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(

        @NotBlank(message = "กรุณาระบุ role")
        @Pattern(
                regexp = "ADMIN|LIBRARIAN|MEMBER",
                message = "role ต้องเป็น ADMIN, LIBRARIAN หรือ MEMBER"
        )
        String role

) {
}