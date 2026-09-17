package com.libraflow.library.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ข้อมูลสำหรับสร้างหนังสือใหม่ ตาม doc/api-spec.md ข้อ 5
 *
 * DTO Pattern — แยก API contract ออกจาก Entity เพื่อ
 *   1. กัน field ที่ไม่ควรหลุดออกไปทาง API (เช่น password ของ User)
 *   2. เปลี่ยน database schema ได้โดยไม่ break frontend
 *   3. รับ id ของความสัมพันธ์เป็นตัวเลข ไม่ต้องให้ client ส่ง object ทั้งก้อนมา
 *
 * ใช้ record เพราะ DTO เป็นข้อมูลอ่านอย่างเดียว — immutable โดยธรรมชาติ
 * ไม่มีใครแก้ค่าระหว่างทางได้ และไม่ต้องเขียน getter เอง
 */
public record CreateBookRequest(

        @NotBlank(message = "ISBN ต้องไม่เป็นค่าว่าง")
        @Pattern(regexp = "\d{10}|\d{13}", message = "ISBN ต้องเป็นตัวเลข 10 หรือ 13 หลัก")
        String isbn,

        @NotBlank(message = "ชื่อหนังสือต้องไม่เป็นค่าว่าง")
        @Size(max = 200, message = "ชื่อหนังสือต้องยาวไม่เกิน 200 ตัวอักษร")
        String title,

        @Min(value = 1000, message = "ปีที่พิมพ์ต้องไม่น้อยกว่า 1000")
        @Max(value = 2100, message = "ปีที่พิมพ์ต้องไม่เกิน 2100")
        Integer publishYear,

        @PositiveOrZero(message = "ราคาต้องไม่ติดลบ")
        BigDecimal price,

        @NotNull(message = "ต้องระบุหมวดหมู่")
        Long categoryId,

        @NotNull(message = "ต้องระบุสำนักพิมพ์")
        Long publisherId,

        @NotEmpty(message = "ต้องระบุผู้แต่งอย่างน้อยหนึ่งคน")
        List<Long> authorIds
) {
}
