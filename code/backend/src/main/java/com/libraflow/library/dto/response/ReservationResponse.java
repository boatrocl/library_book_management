package com.libraflow.library.dto.response;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String status;

    public ReservationResponse(Long id, Long userId, String username, Long bookId, String bookTitle, LocalDateTime reservedAt, LocalDateTime expiresAt, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getStatus() { return status; }
}
