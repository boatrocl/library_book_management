package com.libraflow.library.service.strategy;

import com.libraflow.library.domain.enums.MemberTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** ค่าปรับของ STUDENT: 3 บาทต่อวัน */
@Component
public class StudentFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("3.00");

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        return DAILY_RATE.multiply(BigDecimal.valueOf(Math.max(0, overdueDays)));
    }

    @Override
    public MemberTier getApplicableMemberTier() {
        return MemberTier.STUDENT;
    }
}
