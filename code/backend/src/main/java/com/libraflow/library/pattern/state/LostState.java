package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * สถานะ LOST: ใบยืมที่เลยกำหนดส่งเกิน 60 วัน หรือสูญหาย (BR-08)
 * - อนุญาตให้บันทึกคืนเมื่อชดใช้ค่าหนังสือเต็มราคาแล้ว (LOST -> RETURNED ตาม State Diagram)
 * - ไม่อนุญาตให้ต่ออายุ
 * อ้างอิง doc/diagrams/09-state-loan.puml
 */
@Component
public class LostState implements LoanState {

    @Override
    public LoanStatus status() {
        return LoanStatus.LOST;
    }

    @Override
    public void onReturn(Loan loan, LoanItem item) {
        if (item.isReturned()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "หนังสือเล่มนี้ถูกบันทึกการคืน/ชดใช้ไปแล้ว"
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
                "หนังสืออยู่ในสถานะสูญหาย ไม่สามารถต่ออายุได้ ต้องดำเนินการชดใช้ค่าหนังสือตาม BR-08"
        );
    }

    @Override
    public void onRenew(Loan loan, LoanItem item) {
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "หนังสือรายการนี้อยู่ในสถานะสูญหาย ไม่สามารถต่ออายุได้"
        );
    }
}
