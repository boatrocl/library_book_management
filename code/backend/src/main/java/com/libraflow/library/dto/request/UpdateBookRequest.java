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
 * ข้อมูลสำหรับแก้ไขหนังสือ — ใช้กับ PUT ซึ่งเป็นการแทนที่ทั้งก้อน
 * จึงบังคับให้ส่งทุก field มาครบเหมือนตอนสร้าง (ถ้าต้องการแก้บางส่วนควรใช้ PATCH ซึ่งยังไม่มีในขอบเขตนี้)
 *
 * แยกคลาสจาก CreateBookRequest แทนที่จะใช้ตัวเดียวกัน เพราะสองงานนี้มีโอกาสแตกต่างกันในอนาคต
 * เช่น ตอนสร้างอาจบังคับ ISBN แต่ตอนแก้อาจห้ามเปลี่ยน ISBN
 */
public record UpdateBookRequest(

        @NotBlank(message = "ISBN ต้องไม่เป็นค่าว่าง")
        @Pattern(regexp = "[0-9]{10}|[0-9]{13}", message = "ISBN ต้องเป็นตัวเลข 10 หรือ 13 หลัก")
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
