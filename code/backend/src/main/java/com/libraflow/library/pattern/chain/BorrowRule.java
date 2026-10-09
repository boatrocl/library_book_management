package com.libraflow.library.pattern.chain;

/**
 * Interface สำหรับกฎการตรวจสอบสิทธิ์การยืมหนังสือ (Chain of Responsibility Pattern)
 * อ้างอิง doc/design-patterns.md ข้อ 3.4
 *
 * แต่ละ implementation จะกำหนดลำดับผ่าน order() และโยน BusinessException หากไม่ผ่านกฎ
 */
public interface BorrowRule {

    /**
     * ลำดับของกฎในการตรวจสอบ (เลขน้อยทำงานก่อน)
     * 1 = MemberStatusRule (BR-01)
     * 2 = UnpaidFineRule (BR-02)
     * 3 = LoanQuotaRule (BR-03)
     * 4 = CopyAvailabilityRule (BR-04)
     * 5 = NoDuplicateTitleLoanRule (BR-09)
     */
    int order();

    /**
     * ตรวจสอบเงื่อนไขตามกฎ
     *
     * @param ctx ข้อมูลบริบทการยืม
     * @throws com.libraflow.library.exception.BusinessException เมื่อไม่ผ่านเงื่อนไข
     */
    void check(BorrowContext ctx);
}
