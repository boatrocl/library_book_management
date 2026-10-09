package com.libraflow.library.service;

import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.dto.request.BorrowRequest;
import com.libraflow.library.dto.request.MemberBorrowRequest;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface สำหรับระบบการยืม-คืน (Circulation)
 * แยก Interface ตาม ISP (Interface Segregation Principle)
 */
public interface LoanService {

    /**
     * บันทึกการยืมหนังสือ (ผ่าน BorrowRule chain และคำนวณวันคืนตาม MemberTier)
     */
    LoanResponse borrow(BorrowRequest request);

    /** Borrow the first available copy on behalf of the authenticated member. */
    LoanResponse borrowForMember(String username, MemberBorrowRequest request);

    /**
     * บันทึกการคืนหนังสือ (ผ่าน State Pattern และ publish BookReturnedEvent)
     */
    LoanResponse returnBook(Long loanId);

    /**
     * ต่ออายุใบยืม (BR-06: สูงสุด 2 ครั้ง)
     */
    LoanResponse renewLoan(Long loanId);

    /**
     * ดูรายละเอียดใบยืมตาม id
     */
    LoanResponse getLoanById(Long id);

    /**
     * รายการใบยืมทั้งหมด พร้อมตัวกรองตาม status และ pagination
     */
    PageResponse<LoanResponse> getAllLoans(LoanStatus status, Pageable pageable);

    /**
     * ประวัติการยืมของสมาชิก
     */
    PageResponse<LoanResponse> getMemberLoans(Long memberId, Pageable pageable);

    /**
     * ยกเลิก/ลบใบยืม
     */
    void deleteLoan(Long id);
}
