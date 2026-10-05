package com.libraflow.library.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: สำหรับสมาชิกทั่วไป (ปรับวันละ 5 บาท)
 */
@Component
public class StandardMemberFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("5.00");

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return DAILY_RATE.multiply(new BigDecimal(overdueDays));
    }

    @Override
    public String getApplicableUserRole() {
        return "MEMBER"; // อ้างอิงจาก UserRole ในระบบ
    }
}
