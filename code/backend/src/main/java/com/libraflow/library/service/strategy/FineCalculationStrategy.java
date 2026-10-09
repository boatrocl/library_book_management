package com.libraflow.library.service.strategy;

import com.libraflow.library.domain.enums.MemberTier;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Interface สำหรับการคำนวณค่าปรับ
 * ช่วยให้เราสามารถเพิ่มวิธีการคิดค่าปรับใหม่ๆ ได้โดยไม่ต้องแก้โค้ดเดิม (Open/Closed Principle)
 */
public interface FineCalculationStrategy {

    /**
     * คำนวณค่าปรับจากจำนวนวันที่เกินกำหนด
     * @param overdueDays จำนวนวันที่เกินกำหนด
     * @return จำนวนเงินค่าปรับ
     */
    BigDecimal calculateFine(int overdueDays);

    /** ระบุประเภทสมาชิกที่ Strategy นี้ใช้คำนวณ */
    MemberTier getApplicableMemberTier();
}
