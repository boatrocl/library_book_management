package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.LoanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {

    /** รายการตัวเล่มทั้งหมดในใบยืมใบหนึ่ง */
    List<LoanItem> findByLoanId(Long loanId);

    /**
     * ค้นหารายการยืมที่กำลังดำเนินการอยู่ของตัวเล่มหนึ่งจากบาร์โค้ด
     */
    @Query("""
            SELECT i FROM LoanItem i
            WHERE i.bookCopy.barcode = :barcode
              AND i.returnedAt IS NULL
              AND i.loan.status IN (com.libraflow.library.domain.enums.LoanStatus.ACTIVE, com.libraflow.library.domain.enums.LoanStatus.OVERDUE)
            """)
    Optional<LoanItem> findActiveItemByBarcode(@Param("barcode") String barcode);

    /**
     * ค้นหารายการยืมที่ยังไม่คืนและเลยกำหนดคืน
     */
    @Query("""
            SELECT i FROM LoanItem i
            WHERE i.returnedAt IS NULL
              AND i.dueDate < :today
            """)
    List<LoanItem> findOverdueItems(@Param("today") LocalDate today);
}
