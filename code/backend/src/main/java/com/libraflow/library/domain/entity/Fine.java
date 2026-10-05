package com.libraflow.library.domain.entity;

import com.libraflow.library.domain.enums.FineStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_item_id", nullable = false, unique = true)
    private LoanItem loanItem;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FineStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    protected Fine() {
        // JPA only
    }

    public Fine(LoanItem loanItem, BigDecimal amount, Integer overdueDays) {
        this.loanItem = loanItem;
        this.amount = amount;
        this.overdueDays = overdueDays;
        this.status = FineStatus.UNPAID;
    }

    public Long getId() {
        return id;
    }

    public LoanItem getLoanItem() {
        return loanItem;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public FineStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void markAsPaid() {
        this.status = FineStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void waiveFine() {
        this.status = FineStatus.WAIVED;
        this.paidAt = LocalDateTime.now();
    }
}
