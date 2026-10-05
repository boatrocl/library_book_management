package com.libraflow.library.pattern.chain;

import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * กฎข้อที่ 2 ของ Chain of Responsibility — BR-02: สมาชิกที่มีค่าปรับค้างชำระรวมเกิน 100 บาท ยืมหนังสือไม่ได้
 * อ้างอิง doc/project-overview.md ข้อ 3 และ doc/diagrams/05-sequence-borrow.puml ขั้นที่ 43
 */
@Component
public class UnpaidFineRule implements BorrowRule {

    private static final BigDecimal MAX_ALLOWED_UNPAID_FINE = new BigDecimal("100.00");

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void check(BorrowContext ctx) {
        BigDecimal unpaidFine = ctx.getTotalUnpaidFine();
        if (unpaidFine != null && unpaidFine.compareTo(MAX_ALLOWED_UNPAID_FINE) > 0) {
            throw new BusinessException(
                    ErrorCode.UNPAID_FINE_EXCEEDED,
                    String.format("สมาชิกมีค่าปรับค้างชำระ %.2f บาท เกินเกณฑ์ 100 บาท", unpaidFine)
            );
        }
    }
}
