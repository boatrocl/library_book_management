package com.libraflow.library.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: สำหรับอาจารย์ (ปรับวันละ 2 บาท)
 * (สร้างไว้เพื่อแสดงให้เห็นการประยุกต์ใช้ Strategy Pattern)
 */
@Component
public class FacultyMemberFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("2.00");

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return DAILY_RATE.multiply(new BigDecimal(overdueDays));
    }

    @Override
    public String getApplicableUserRole() {
        return "FACULTY"; // สมมติว่ามี Role สำหรับอาจารย์
    }
}
