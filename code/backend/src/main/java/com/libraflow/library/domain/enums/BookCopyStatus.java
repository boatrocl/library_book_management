package com.libraflow.library.domain.enums;

/**
 * สถานะของตัวเล่มหนังสือ — ดูผังการเปลี่ยนสถานะที่ doc/diagrams/10-state-bookcopy.puml
 * ใช้ใน CopyAvailabilityRule ของ Chain of Responsibility เพื่อตัดสินว่ายืมได้หรือไม่ (BR-04)
 */
public enum BookCopyStatus {

    /** อยู่บนชั้น พร้อมให้ยืม */
    AVAILABLE,

    /** ถูกยืมออกไปแล้ว */
    ON_LOAN,

    /** กันไว้ให้สมาชิกที่จองคิวลำดับแรกมารับภายใน 48 ชม. (BR-10) */
    RESERVED,

    /** ชำรุด รอซ่อมหรือจำหน่ายออก */
    DAMAGED,

    /** สูญหาย เรียกเก็บค่าหนังสือเต็มราคาแล้ว (BR-08) */
    LOST
}
