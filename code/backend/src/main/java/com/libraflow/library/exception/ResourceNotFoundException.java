package com.libraflow.library.exception;

/**
 * ไม่พบข้อมูลที่ระบุ -> 404
 *
 * แยกออกมาจาก BusinessException เพราะ "หาไม่เจอ" เป็นคนละความหมายกับ
 * "เจอแล้วแต่ทำไม่ได้เพราะผิดกฎธุรกิจ" (409) และทำให้ handler แยกจัดการได้ชัด
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("ไม่พบ " + resourceName + " ที่มี id = " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
