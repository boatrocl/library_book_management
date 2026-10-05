package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.LoanStatus;

/**
 * Interface สำหรับ State Pattern ของใบยืม (Loan)
 * อ้างอิง doc/design-patterns.md ข้อ 3.2 และ doc/diagrams/09-state-loan.puml
 */
public interface LoanState {

    /**
     * สถานะของใบยืมที่ State นี้ดูแล
     */
    LoanStatus status();

    /**
     * จัดการตรรกะและเงื่อนไขเมื่อมีการคืนหนังสือ
     *
     * @param loan ใบยืมต้นทาง
     * @param item รายการตัวเล่มที่นำมาคืน
     */
    void onReturn(Loan loan, LoanItem item);

    /**
     * จัดการตรรกะและเงื่อนไขเมื่อมีการต่ออายุใบยืมทั้งใบ (BR-06)
     *
     * @param loan ใบยืมที่ต้องการต่ออายุ
     */
    void onRenew(Loan loan);

    /**
     * จัดการตรรกะและเงื่อนไขเมื่อมีการต่ออายุเฉพาะรายการตัวเล่ม
     *
     * @param loan ใบยืมต้นทาง
     * @param item รายการตัวเล่มที่ต้องการต่ออายุ
     */
    default void onRenew(Loan loan, LoanItem item) {
        onRenew(loan);
    }
}
