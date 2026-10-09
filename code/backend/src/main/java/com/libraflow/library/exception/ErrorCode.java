package com.libraflow.library.exception;

import org.springframework.http.HttpStatus;

/**
 * รหัสข้อผิดพลาดทั้งระบบ
 * ตรงกับตารางใน doc/api-spec.md ข้อ 4
 *
 * การผูก HttpStatus ไว้กับ enum
 * ทำให้ GlobalExceptionHandler
 * ไม่ต้องมี switch/if-else
 */
public enum ErrorCode {

    // ---------- ทั่วไป ----------

    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "ข้อมูลที่ส่งมาไม่ถูกต้อง"
    ),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ไม่พบข้อมูลที่ระบุ"
    ),

    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "ไม่มีสิทธิ์เข้าถึง"
    ),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "เกิดข้อผิดพลาดที่ไม่คาดคิด"
    ),

    // ---------- Catalog (สมาชิกคนที่ 1) ----------

    ISBN_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "ISBN นี้มีอยู่ในระบบแล้ว"
    ),

    BARCODE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "บาร์โค้ดนี้มีอยู่ในระบบแล้ว"
    ),

    BOOK_IN_USE(
            HttpStatus.CONFLICT,
            "ลบหนังสือไม่ได้เพราะยังมีตัวเล่มถูกยืมหรือถูกจองอยู่"
    ),

    // ---------- Loan (สมาชิกคนที่ 2) ----------

    MEMBER_SUSPENDED(
            HttpStatus.CONFLICT,
            "บัญชีสมาชิกถูกระงับ"
    ),

    UNPAID_FINE_EXCEEDED(
            HttpStatus.CONFLICT,
            "ค่าปรับค้างชำระเกินเกณฑ์"
    ),

    LOAN_QUOTA_EXCEEDED(
            HttpStatus.CONFLICT,
            "ยืมครบโควต้าแล้ว"
    ),

    COPY_NOT_AVAILABLE(
            HttpStatus.CONFLICT,
            "ตัวเล่มไม่พร้อมให้ยืม"
    ),

    BOOK_COPIES_AVAILABLE(
            HttpStatus.CONFLICT,
            "ยังมีตัวเล่มพร้อมให้ยืม กรุณายืมหนังสือก่อน"
    ),

    RENEW_LIMIT_REACHED(
            HttpStatus.CONFLICT,
            "ต่ออายุครบจำนวนครั้งแล้ว"
    ),

    RENEW_BLOCKED_BY_RESERVATION(
            HttpStatus.CONFLICT,
            "มีผู้จองคิวรออยู่ ต่ออายุไม่ได้"
    ),

    // ---------- Reservation / Fine (สมาชิกคนที่ 3) ----------

    DUPLICATE_RESERVATION(
            HttpStatus.CONFLICT,
            "จองหนังสือเล่มนี้ซ้ำไม่ได้"
    ),

    RESERVATION_NOT_CANCELLABLE(
            HttpStatus.CONFLICT,
            "ยกเลิกได้เฉพาะรายการจองที่ยัง WAITING หรือ READY"
    );

    private final HttpStatus status;

    private final String defaultMessage;

    ErrorCode(
            HttpStatus status,
            String defaultMessage
    ) {

        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
