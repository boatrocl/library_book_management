package com.libraflow.library.exception;

/**
 * ข้อผิดพลาดที่เกิดจากกฎทางธุรกิจ (BR-xx) ไม่ใช่ข้อผิดพลาดทางเทคนิค
 *
 * สืบทอด RuntimeException เพื่อไม่ต้องประกาศ throws ทุกชั้น และเพื่อให้ Spring
 * rollback transaction อัตโนมัติ (@Transactional จะ rollback เฉพาะ unchecked exception)
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /** ใช้เมื่ออยากใส่รายละเอียดเพิ่ม เช่น "ค่าปรับค้างชำระ 150.00 บาท เกินเกณฑ์ 100 บาท" */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
