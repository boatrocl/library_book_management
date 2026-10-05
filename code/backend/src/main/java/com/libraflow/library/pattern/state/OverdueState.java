package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * สถานะ OVERDUE: ใบยืมที่เลยกำหนดส่งคืนแล้ว
 * - อนุญาตให้คืนหนังสือได้ (แต่จะมีการคำนวณค่าปรับตาม BR-07)
 * - ไม่อนุญาตให้ต่ออายุ (ต้องโยน BusinessException ตามหลัก LSP)
 * อ้างอิง doc/diagrams/09-state-loan.puml
 */
@Component
public class OverdueState implements LoanState {

    @Override
    public LoanStatus status() {
        return LoanStatus.OVERDUE;
    }

    @Override
    public void onReturn(Loan loan, LoanItem item) {
        if (item.isReturned()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "หนังสือเล่มนี้ถูกบันทึกการคืนไปแล้ว"
            );
        }

        item.setReturnedAt(LocalDate.now());

        boolean allReturned = loan.getItems().stream().allMatch(LoanItem::isReturned);
        if (allReturned) {
            loan.setStatus(LoanStatus.RETURNED);
        }
    }

    @Override
    public void onRenew(Loan loan) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "ใบยืมนี้เกินกำหนดส่งคืนแล้ว ไม่สามารถต่ออายุได้ กรุณาคืนหนังสือและชำระค่าปรับ"
        );
    }

    @Override
    public void onRenew(Loan loan, LoanItem item) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "หนังสือรายการนี้เกินกำหนดส่งคืนแล้ว ไม่สามารถต่ออายุได้"
        );
    }
}
