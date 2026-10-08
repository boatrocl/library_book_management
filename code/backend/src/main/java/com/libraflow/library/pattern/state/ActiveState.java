package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * สถานะ ACTIVE: ใบยืมที่กำลังดำเนินการอยู่ตามปกติ
 * อนุญาตให้คืนหนังสือ และต่ออายุได้ตามเงื่อนไข BR-06
 * อ้างอิง doc/diagrams/09-state-loan.puml
 */
@Component
public class ActiveState implements LoanState {

    @Override
    public LoanStatus status() {
        return LoanStatus.ACTIVE;
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

        // ตรวจสอบว่าทุกเล่มในใบยืมคืนหมดแล้วหรือยัง หากคืนครบทุกเล่มแล้ว ให้เปลี่ยนสถานะของใบยืมเป็น RETURNED
        boolean allReturned = loan.getItems().stream().allMatch(LoanItem::isReturned);
        if (allReturned) {
            loan.setStatus(LoanStatus.RETURNED);
        }
    }

    @Override
    public void onRenew(Loan loan) {
        boolean hasRenewableItem = false;

        for (LoanItem item : loan.getItems()) {
            if (!item.isReturned()) {
                if (item.getRenewCount() >= 2) {
                    throw new BusinessException(
                            ErrorCode.RENEW_LIMIT_REACHED,
                            "ต่ออายุครบจำนวนครั้งแล้ว (ต่ออายุได้ไม่เกิน 2 ครั้งตาม BR-06)"
                    );
                }
                hasRenewableItem = true;
                item.renew(item.getDueDate().plusDays(7));
            }
        }

        if (!hasRenewableItem) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "ไม่มีรายการหนังสือที่สามารถต่ออายุได้ในใบยืมนี้"
            );
        }
    }

    @Override
    public void onRenew(Loan loan, LoanItem item) {
        if (item.isReturned()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "ไม่สามารถต่ออายุหนังสือที่คืนแล้วได้"
            );
        }

        if (item.getRenewCount() >= 2) {
            throw new BusinessException(
                    ErrorCode.RENEW_LIMIT_REACHED,
                    "ต่ออายุครบจำนวนครั้งแล้ว (ต่ออายุได้ไม่เกิน 2 ครั้งตาม BR-06)"
            );
        }

        item.renew(item.getDueDate().plusDays(7));
    }
}
