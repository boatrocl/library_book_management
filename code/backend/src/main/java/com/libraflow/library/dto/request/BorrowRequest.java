package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * ข้อมูลสำหรับบันทึกการยืมหนังสือ (POST /api/v1/loans)
 * อ้างอิง doc/api-spec.md ข้อ 2
 *
 * DTO Pattern — ใช้ record เพื่อความ immutable
 */
public record BorrowRequest(

        @NotNull(message = "ต้องระบุรหัสสมาชิกผู้ยืม")
        Long memberId,

        @NotEmpty(message = "ต้องระบุบาร์โค้ดหนังสืออย่างน้อย 1 เล่ม")
        List<String> barcodes
) {
}
