package com.libraflow.library.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ข้อมูลสำหรับเพิ่มตัวเล่มใหม่ให้หนังสือเล่มหนึ่ง
 *
 * barcode เว้นว่างได้ — ถ้าไม่ส่งมา ระบบจะสร้างให้เองด้วย BarcodeGenerator
 * acquiredAt เว้นว่างได้ — ถ้าไม่ส่งมาจะใช้วันที่ปัจจุบัน
 */
public record CreateBookCopyRequest(

        @Size(max = 30, message = "บาร์โค้ดต้องยาวไม่เกิน 30 ตัวอักษร")
        String barcode,

        @Size(max = 30, message = "ตำแหน่งชั้นวางต้องยาวไม่เกิน 30 ตัวอักษร")
        String shelfLocation,

        LocalDate acquiredAt
) {
}
