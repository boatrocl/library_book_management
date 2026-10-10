package com.libraflow.library.domain.enums;

/**
 * สถานะของใบยืม (Loan) — ดูผังการเปลี่ยนสถานะที่ doc/diagrams/09-state-loan.puml
 * ควบคุมพฤติกรรมผ่าน State Pattern (pattern/state/LoanState.java)
 */
public enum LoanStatus {

    /** อยู่ระหว่างการยืม (ยังไม่ครบกำหนด หรือยังไม่ถูกเปลี่ยนเป็นสถานะอื่น) */
    ACTIVE,

    /** เกินกำหนดส่งคืน; scheduler เปลี่ยนสถานะ ส่วนค่าปรับบันทึกเมื่อคืนตาม BR-07 */
    OVERDUE,

    /** คืนหนังสือครบถ้วนแล้ว */
    RETURNED,

    /** เกินกำหนดส่งคืนเกิน 60 วัน หรือสูญหาย (เรียกเก็บค่าหนังสือเต็มราคาตาม BR-08) */
    LOST
}
