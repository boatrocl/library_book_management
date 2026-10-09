package com.libraflow.library.service.strategy;

import com.libraflow.library.domain.enums.MemberTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** ค่าปรับของ STAFF: 5 บาทต่อวัน สูงสุด 300 บาท */
@Component
public class StaffFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("5.00");
    private static final BigDecimal MAX_FINE = new BigDecimal("300.00");

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        BigDecimal amount = DAILY_RATE.multiply(BigDecimal.valueOf(Math.max(0, overdueDays)));
        return amount.min(MAX_FINE);
    }

    @Override
    public MemberTier getApplicableMemberTier() {
        return MemberTier.STAFF;
    }
}
