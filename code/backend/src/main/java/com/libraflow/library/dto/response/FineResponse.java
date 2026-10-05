package com.libraflow.library.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FineResponse {
    private Long id;
    private Long loanItemId;
    private BigDecimal amount;
    private Integer overdueDays;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public FineResponse(Long id, Long loanItemId, BigDecimal amount, Integer overdueDays, String status, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.loanItemId = loanItemId;
        this.amount = amount;
        this.overdueDays = overdueDays;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public Long getId() { return id; }
    public Long getLoanItemId() { return loanItemId; }
    public BigDecimal getAmount() { return amount; }
    public Integer getOverdueDays() { return overdueDays; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
