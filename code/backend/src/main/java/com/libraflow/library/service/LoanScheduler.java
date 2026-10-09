package com.libraflow.library.service;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.repository.LoanRepository;
import com.libraflow.library.service.FineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled Job สำหรับตรวจสอบและปรับปรุงสถานะใบยืม (Loan) และตัวเล่มหนังสือ (BookCopy) อัตโนมัติ
 *
 * การทำงานหลัก:
 * 1. ตรวจสอบรายการยืมที่ครบกำหนดทุกเที่ยงคืนเพื่อเปลี่ยนสถานะจาก ACTIVE → OVERDUE (เมื่อ today > dueDate)
 * 2. ตรวจสอบรายการยืมที่เลยกำหนดส่งคืนเกิน 60 วัน เพื่อเปลี่ยนสถานะจาก OVERDUE → LOST ตาม BR-08
 *    พร้อมปรับสถานะตัวเล่มหนังสือ (BookCopy) เป็น LOST
 *
 * อ้างอิง:
 * - doc/diagrams/09-state-loan.puml
 * - doc/diagrams/10-state-bookcopy.puml
 * - BR-07, BR-08
 */
@Component
@EnableScheduling
public class LoanScheduler {

    private static final Logger log = LoggerFactory.getLogger(LoanScheduler.class);

    private final LoanRepository loanRepository;
    private final FineService fineService;

    public LoanScheduler(LoanRepository loanRepository, FineService fineService) {
        this.loanRepository = loanRepository;
        this.fineService = fineService;
    }

    /**
     * รันตรวจสอบสถานะใบยืมทุกเที่ยงคืน (00:00:00 น.) ตามกำหนด
     */
    @Scheduled(cron = "${libraflow.loan.scheduler.cron:0 0 0 * * ?}")
    @Transactional
    public void runMidnightLoanStatusCheck() {
        log.info("Starting midnight loan status check job...");
        int overdueCount = detectAndProcessOverdueLoans();
        int lostCount = detectAndProcessLostLoans();
        log.info("Midnight loan status check completed. Updated to OVERDUE: {}, Updated to LOST: {}",
                overdueCount, lostCount);
    }

    /**
     * ค้นหาใบยืมที่ยังเป็น ACTIVE แต่มีรายการที่เลยกำหนดคืนแล้ว (today > dueDate)
     * ปรับสถานะใบยืมเป็น OVERDUE
     *
     * @return จำนวนใบยืมที่ถูกเปลี่ยนสถานะเป็น OVERDUE
     */
    @Transactional
    public int detectAndProcessOverdueLoans() {
        return detectAndProcessOverdueLoans(LocalDate.now());
    }

    /**
     * ค้นหาและปรับสถานะใบยืมที่เลยกำหนดส่งตามวันที่ระบุ (รองรับการทดสอบ Unit Test)
     *
     * @param today วันที่ใช้เป็นฐานในการตรวจสอบ
     * @return จำนวนใบยืมที่ถูกเปลี่ยนสถานะเป็น OVERDUE
     */
    @Transactional
    public int detectAndProcessOverdueLoans(LocalDate today) {
        List<Loan> overdueLoans = loanRepository.findActiveLoansWithOverdueItems(today);
        if (overdueLoans.isEmpty()) {
            log.debug("No active loans found with overdue items on {}", today);
            return 0;
        }

        for (Loan loan : overdueLoans) {
            loan.setStatus(LoanStatus.OVERDUE);
            log.warn("Loan id {} (code: {}) has overdue items. Status updated from ACTIVE to OVERDUE.",
                    loan.getId(), loan.getLoanCode());
        }

        loanRepository.saveAll(overdueLoans);
        return overdueLoans.size();
    }

    /**
     * ค้นหาใบยืมที่เป็น OVERDUE และเลยกำหนดเกิน 60 วัน (overdueDays > 60)
     * ปรับสถานะใบยืมเป็น LOST และปรับสถานะ BookCopy ที่ยังไม่คืนเป็น LOST ตาม BR-08
     *
     * @return จำนวนใบยืมที่ถูกเปลี่ยนสถานะเป็น LOST
     */
    @Transactional
    public int detectAndProcessLostLoans() {
        return detectAndProcessLostLoans(LocalDate.now());
    }

    /**
     * ค้นหาและปรับสถานะใบยืมที่เกิน 60 วันเป็น LOST ตามวันที่ระบุ (รองรับการทดสอบ Unit Test)
     *
     * @param today วันที่ใช้เป็นฐานในการตรวจสอบ
     * @return จำนวนใบยืมที่ถูกเปลี่ยนสถานะเป็น LOST
     */
    @Transactional
    public int detectAndProcessLostLoans(LocalDate today) {
        LocalDate cutoffDate = today.minusDays(60);
        List<Loan> lostLoans = loanRepository.findOverdueLoansExceedingDays(cutoffDate);
        if (lostLoans.isEmpty()) {
            log.debug("No overdue loans exceeding 60 days on {}", today);
            return 0;
        }

        for (Loan loan : lostLoans) {
            loan.setStatus(LoanStatus.LOST);
            for (LoanItem item : loan.getItems()) {
                if (!item.isReturned()) {
                    BookCopy copy = item.getBookCopy();
                    if (copy != null && copy.getStatus() == BookCopyStatus.ON_LOAN) {
                        int overdueDays = Math.toIntExact(Math.max(
                                0,
                                ChronoUnit.DAYS.between(item.getDueDate(), today)
                        ));
                        fineService.generateLostBookFine(item, overdueDays);
                        copy.setStatus(BookCopyStatus.LOST);
                        log.info("BookCopy id {} (barcode: {}) updated to LOST for loan id {}",
                                copy.getId(), copy.getBarcode(), loan.getId());
                    }
                }
            }
            log.warn("Loan id {} (code: {}) has been overdue for > 60 days. Status updated from OVERDUE to LOST (BR-08).",
                    loan.getId(), loan.getLoanCode());
        }

        loanRepository.saveAll(lostLoans);
        return lostLoans.size();
    }
}
