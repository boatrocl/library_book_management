package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** ค้นหารายการใบยืมตามรหัส เช่น LN-20260912-0007 */
    Optional<Loan> findByLoanCode(String loanCode);

    /** ตรวจสอบว่ามีรหัสใบยืมนี้อยู่แล้วหรือไม่ */
    boolean existsByLoanCode(String loanCode);

    /**
     * ดึงใบยืมพร้อมโหลด user, librarian และ items พร้อม bookCopy
     * ใช้ตอนดูรายละเอียด หรือตอนคืน/ต่ออายุหนังสือ เพื่อเลี่ยง N+1
     * (ดู doc/diagrams/06-sequence-return.puml ขั้นที่ 32)
     */
    @Query("""
            SELECT l FROM Loan l
            LEFT JOIN FETCH l.user u
            LEFT JOIN FETCH l.librarian lib
            LEFT JOIN FETCH l.items i
            LEFT JOIN FETCH i.bookCopy bc
            LEFT JOIN FETCH bc.book b
            WHERE l.id = :id
            """)
    Optional<Loan> findByIdWithDetails(@Param("id") Long id);

    /**
     * ค้นหาใบยืมตาม status หรือค้นหาทั้งหมดถ้า status เป็น null พร้อมแบ่งหน้า
     */
    @Query("""
            SELECT l FROM Loan l
            WHERE (:status IS NULL OR l.status = :status)
            """)
    Page<Loan> findAllByStatus(@Param("status") LoanStatus status, Pageable pageable);

    /**
     * ดูประวัติการยืมของสมาชิกคนหนึ่ง (GET /api/v1/members/{id}/loans)
     */
    Page<Loan> findByUserId(Long userId, Pageable pageable);

    /**
     * นับจำนวนตัวเล่มที่สมาชิกคนนี้กำลังยืมอยู่และยังไม่ได้คืน (สถานะ ACTIVE หรือ OVERDUE)
     * ใช้ใน LoanQuotaRule (BR-03) ของ Chain of Responsibility
     */
    @Query("""
            SELECT COUNT(i) FROM LoanItem i
            WHERE i.loan.user.id = :userId
              AND i.loan.status IN (com.libraflow.library.domain.enums.LoanStatus.ACTIVE, com.libraflow.library.domain.enums.LoanStatus.OVERDUE)
              AND i.returnedAt IS NULL
            """)
    long countActiveLoanItemsByUserId(@Param("userId") Long userId);

    /**
     * ค้นหาใบยืมที่ยังเป็น ACTIVE แต่มีรายการที่เลยกำหนดคืนแล้ว (สำหรับ Scheduled Job ตรวจสอบทุกเที่ยงคืน)
     */
    @Query("""
            SELECT DISTINCT l FROM Loan l
            JOIN l.items i
            WHERE l.status = com.libraflow.library.domain.enums.LoanStatus.ACTIVE
              AND i.returnedAt IS NULL
              AND i.dueDate < :today
            """)
    List<Loan> findActiveLoansWithOverdueItems(@Param("today") LocalDate today);

    /**
     * ค้นหาใบยืมที่เป็น OVERDUE และเลยกำหนดเกิน 60 วัน เพื่อปรับเป็น LOST (BR-08)
     */
    @Query("""
            SELECT DISTINCT l FROM Loan l
            JOIN l.items i
            WHERE l.status = com.libraflow.library.domain.enums.LoanStatus.OVERDUE
              AND i.returnedAt IS NULL
              AND i.dueDate < :cutoffDate
            """)
    List<Loan> findOverdueLoansExceedingDays(@Param("cutoffDate") LocalDate cutoffDate);
}
