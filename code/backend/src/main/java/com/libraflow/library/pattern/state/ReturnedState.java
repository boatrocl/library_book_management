package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * สถานะ RETURNED: ใบยืมที่คืนหนังสือครบถ้วนแล้ว
 *
 * SOLID - L (Liskov Substitution Principle):
 * เมื่อพยายามทำ Action ที่ไม่อนุญาตในสถานะนี้ (เช่น คืนซ้ำ หรือต่ออายุ)
 * ต้องโยน BusinessException ซึ่งเป็นข้อผิดพลาดเชิงธุรกิจที่สอดคล้องกับ contract
 * ไม่โยน UnsupportedOperationException
 * อ้างอิง doc/solid-analysis.md ข้อ L และ doc/diagrams/09-state-loan.puml
 */
@Component
public class ReturnedState implements LoanState {

    @Override
    public LoanStatus status() {
        return LoanStatus.RETURNED;
    }

    @Override
    public void onReturn(Loan loan, LoanItem item) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "ใบยืมนี้คืนหนังสือครบถ้วนแล้ว ไม่สามารถบันทึกการคืนซ้ำได้"
        );
    }

    @Override
    public void onRenew(Loan loan) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "ใบยืมนี้คืนหนังสือแล้ว ไม่สามารถต่ออายุได้"
        );
    }

    @Override
    public void onRenew(Loan loan, LoanItem item) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "หนังสือรายการนี้ถูกคืนแล้ว ไม่สามารถต่ออายุได้"
        );
    }
}
