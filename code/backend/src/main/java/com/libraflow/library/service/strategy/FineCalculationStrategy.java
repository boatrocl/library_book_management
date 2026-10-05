package com.libraflow.library.service.strategy;

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

    /**
     * ระบุว่า Strategy นี้ใช้สำหรับ Role หรือเงื่อนไขไหน
     * @return ชื่อของประเภทที่รองรับ
     */
    String getApplicableUserRole();
}
