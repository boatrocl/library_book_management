package com.libraflow.library.service.strategy;

import com.libraflow.library.domain.enums.MemberTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** ค่าปรับของ EXTERNAL: 10 บาทต่อวัน */
@Component
public class ExternalFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("10.00");

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        return DAILY_RATE.multiply(BigDecimal.valueOf(Math.max(0, overdueDays)));
    }

    @Override
    public MemberTier getApplicableMemberTier() {
        return MemberTier.EXTERNAL;
    }
}
