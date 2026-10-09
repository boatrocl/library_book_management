package com.libraflow.library.service.strategy;

import com.libraflow.library.domain.enums.MemberTier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FineCalculationStrategyTest {

    @Test
    void studentFineIsThreeBahtPerOverdueDay() {
        StudentFineStrategy strategy = new StudentFineStrategy();
        assertEquals(new BigDecimal("15.00"), strategy.calculateFine(5));
        assertEquals(new BigDecimal("0.00"), strategy.calculateFine(0));
        assertEquals(MemberTier.STUDENT, strategy.getApplicableMemberTier());
    }

    @Test
    void staffFineIsFiveBahtPerDayAndCappedAtThreeHundredBaht() {
        StaffFineStrategy strategy = new StaffFineStrategy();
        assertEquals(new BigDecimal("25.00"), strategy.calculateFine(5));
        assertEquals(new BigDecimal("300.00"), strategy.calculateFine(90));
        assertEquals(MemberTier.STAFF, strategy.getApplicableMemberTier());
    }

    @Test
    void externalFineIsTenBahtPerOverdueDay() {
        ExternalFineStrategy strategy = new ExternalFineStrategy();
        assertEquals(new BigDecimal("50.00"), strategy.calculateFine(5));
        assertEquals(MemberTier.EXTERNAL, strategy.getApplicableMemberTier());
    }
}
