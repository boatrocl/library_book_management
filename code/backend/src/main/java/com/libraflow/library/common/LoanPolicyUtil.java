package com.libraflow.library.common;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.domain.enums.UserRole;

import java.time.LocalDate;

/**
 * Utility คลาสสำหรับจัดการและคำนวณนโยบายการยืมหนังสือ (Loan Policy) และวันครบกำหนดส่งคืน (Due Date)
 *
 * อ้างอิงข้อกำหนดทางธุรกิจ:
 * - BR-03: โควต้าการยืมตาม Tier (STUDENT: 5, STAFF: 10, EXTERNAL: 2)
 * - BR-05: ระยะเวลายืมตาม Tier (STUDENT: 7 วัน, STAFF: 14 วัน, EXTERNAL: 3 วัน)
 * - BR-06: การต่ออายุ ขยายเวลาได้ครั้งละ 7 วัน ไม่เกิน 2 ครั้ง
 */
public final class LoanPolicyUtil {

    public static final int DEFAULT_RENEW_DAYS = 7;
    public static final int MAX_RENEW_COUNT = 2;

    private LoanPolicyUtil() {
        // Private constructor สำหรับ Utility class เพื่อป้องกันการสร้าง instance
    }

    /**
     * กำหนด MemberTier จากข้อมูลของ User
     * - บุคลากร/ผู้ดูแล (ADMIN, LIBRARIAN) -> STAFF
     * - สมาชิกทั่วไป (MEMBER หรืออื่น ๆ) -> STUDENT
     */
    public static MemberTier resolveMemberTier(User member) {
        if (member == null) {
            return MemberTier.STUDENT;
        }
        if (member.getRole() == UserRole.ADMIN || member.getRole() == UserRole.LIBRARIAN) {
            return MemberTier.STAFF;
        }
        return MemberTier.STUDENT;
    }

    /**
     * คำนวณวันครบกำหนดคืน (Due Date) ตาม MemberTier จากวันที่ปัจจุบัน (BR-05)
     */
    public static LocalDate calculateDueDate(MemberTier tier) {
        return calculateDueDate(tier, LocalDate.now());
    }

    /**
     * คำนวณวันครบกำหนดคืน (Due Date) ตาม MemberTier จากวันที่เริ่มต้นที่กำหนด (BR-05)
     */
    public static LocalDate calculateDueDate(MemberTier tier, LocalDate fromDate) {
        LocalDate baseDate = fromDate != null ? fromDate : LocalDate.now();
        MemberTier resolvedTier = tier != null ? tier : MemberTier.STUDENT;
        return baseDate.plusDays(resolvedTier.getLoanDurationDays());
    }

    /**
     * คำนวณวันครบกำหนดคืนใหม่สำหรับการต่ออายุ (Renew) โดยบวกเพิ่ม 7 วันจากกำหนดคืนเดิม (BR-06)
     */
    public static LocalDate calculateRenewDueDate(LocalDate currentDueDate) {
        LocalDate baseDate = currentDueDate != null ? currentDueDate : LocalDate.now();
        return baseDate.plusDays(DEFAULT_RENEW_DAYS);
    }

    /**
     * ดึงระยะเวลาการยืม (จำนวนวัน) ตามประเภทสมาชิก (BR-05)
     */
    public static int getLoanDurationDays(MemberTier tier) {
        return (tier != null ? tier : MemberTier.STUDENT).getLoanDurationDays();
    }

    /**
     * ดึงโควต้าการยืม (จำนวนเล่ม) ตามประเภทสมาชิก (BR-03)
     */
    public static int getLoanQuota(MemberTier tier) {
        return (tier != null ? tier : MemberTier.STUDENT).getLoanQuota();
    }
}
