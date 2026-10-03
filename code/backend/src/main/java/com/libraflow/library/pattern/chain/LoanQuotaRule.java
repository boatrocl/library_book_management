package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * กฎข้อที่ 3 ของ Chain of Responsibility — BR-03: โควต้าการยืมพร้อมกันขึ้นกับ Tier
 * STUDENT 5 เล่ม, STAFF 10 เล่ม, EXTERNAL 2 เล่ม
 * อ้างอิง doc/project-overview.md ข้อ 3 และ doc/diagrams/05-sequence-borrow.puml ขั้นที่ 44
 */
@Component
public class LoanQuotaRule implements BorrowRule {

    @Override
    public int order() {
        return 3;
    }

    @Override
    public void check(BorrowContext ctx) {
        MemberTier tier = ctx.getMemberTier() != null ? ctx.getMemberTier() : MemberTier.STUDENT;
        int quota = tier.getLoanQuota();
        long totalLoans = ctx.getCurrentActiveLoanCount() + ctx.getRequestedCount();

        if (totalLoans > quota) {
            throw new BusinessException(
                    ErrorCode.LOAN_QUOTA_EXCEEDED,
                    String.format(
                            "จำนวนหนังสือที่ยืม (%d เล่มเดิม + %d เล่มใหม่ = %d เล่ม) เกินโควต้าของประเภทสมาชิก %s ซึ่งยืมได้สูงสุด %d เล่ม",
                            ctx.getCurrentActiveLoanCount(),
                            ctx.getRequestedCount(),
                            totalLoans,
                            tier.name(),
                            quota
                    )
            );
        }
    }
}
