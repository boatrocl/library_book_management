package com.libraflow.library.service.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardMemberFineStrategyTest {

    private final StandardMemberFineStrategy strategy = new StandardMemberFineStrategy();

    @Test
    void shouldCalculateFineCorrectlyForOverdueDays() {
        // จัดเตรียมข้อมูล (Arrange)
        int overdueDays = 3;

        // ทดสอบ (Act)
        BigDecimal fine = strategy.calculateFine(overdueDays);

        // ตรวจสอบ (Assert) - 3 วัน * 5 บาท = 15 บาท
        assertEquals(new BigDecimal("15.00"), fine);
    }

    @Test
    void shouldReturnZeroForNoOverdueDays() {
        assertEquals(BigDecimal.ZERO, strategy.calculateFine(0));
        assertEquals(BigDecimal.ZERO, strategy.calculateFine(-2));
    }
}
