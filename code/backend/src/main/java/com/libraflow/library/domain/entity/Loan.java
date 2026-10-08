package com.libraflow.library.domain.entity;

import com.libraflow.library.domain.enums.LoanStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ใบยืมหนังสือ (Loan) — แมปกับตาราง loans ตาม doc/data-dictionary.md ข้อ 9
 * หนึ่งใบยืมประกอบด้วยรายการตัวเล่มที่ยืมหลายเล่ม ({@link LoanItem})
 * มีความสัมพันธ์แบบ One-to-Many + cascade = ALL + orphanRemoval
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_code", nullable = false, unique = true, length = 20)
    private String loanCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id")
    private User librarian;

    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @OneToMany(
            mappedBy = "loan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<LoanItem> items = new ArrayList<>();

    protected Loan() {
        // no-arg constructor สำหรับ JPA
    }

    public Loan(String loanCode, User user, User librarian, LocalDateTime loanDate, LoanStatus status) {
        this.loanCode = loanCode;
        this.user = user;
        this.librarian = librarian;
        this.loanDate = loanDate != null ? loanDate : LocalDateTime.now();
        this.status = status != null ? status : LoanStatus.ACTIVE;
    }

    public void addItem(LoanItem item) {
        items.add(item);
        item.setLoan(this);
    }

    public void removeItem(LoanItem item) {
        items.remove(item);
        item.setLoan(null);
    }

    public Long getId() {
        return id;
    }

    public String getLoanCode() {
        return loanCode;
    }

    public void setLoanCode(String loanCode) {
        this.loanCode = loanCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getLibrarian() {
        return librarian;
    }

    public void setLibrarian(User librarian) {
        this.librarian = librarian;
    }

    public LocalDateTime getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDateTime loanDate) {
        this.loanDate = loanDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public List<LoanItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Loan other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
