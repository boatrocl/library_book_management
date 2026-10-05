package com.libraflow.library.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * รายการตัวเล่มในใบยืม (LoanItem) — แมปกับตาราง loan_items ตาม doc/data-dictionary.md ข้อ 10
 * เป็น composition ของ {@link Loan} ไม่มีความหมายเมื่อไม่มีใบยืม
 */
@Entity
@Table(name = "loan_items")
public class LoanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Column(name = "renew_count", nullable = false)
    private short renewCount = 0;

    protected LoanItem() {
        // no-arg constructor สำหรับ JPA
    }

    public LoanItem(Loan loan, BookCopy bookCopy, LocalDate dueDate) {
        this.loan = loan;
        this.bookCopy = bookCopy;
        this.dueDate = dueDate;
        this.renewCount = 0;
    }

    public Long getId() {
        return id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public short getRenewCount() {
        return renewCount;
    }

    public void setRenewCount(short renewCount) {
        this.renewCount = renewCount;
    }

    /**
     * คืนตัวเล่มแล้วหรือยัง (NULL = ยังไม่คืน)
     */
    public boolean isReturned() {
        return returnedAt != null;
    }

    /**
     * เกินกำหนดส่งคืนหรือไม่ ณ วันที่ระบุ
     */
    public boolean isOverdue(LocalDate asOfDate) {
        return !isReturned() && asOfDate.isAfter(dueDate);
    }

    /**
     * ต่ออายุได้หรือไม่ (ไม่เกิน 2 ครั้งตาม BR-06 และยังไม่คืน)
     */
    public boolean canRenew() {
        return !isReturned() && renewCount < 2;
    }

    /**
     * บันทึกการต่ออายุ เพิ่มจำนวนครั้งและขยาย dueDate
     */
    public void renew(LocalDate newDueDate) {
        this.dueDate = newDueDate;
        this.renewCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoanItem other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
