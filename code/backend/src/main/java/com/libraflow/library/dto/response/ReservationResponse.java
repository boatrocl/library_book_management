package com.libraflow.library.dto.response;

import com.libraflow.library.domain.entity.Reservation;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private Long reservedCopyId;
    private String reservedBarcode;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String status;

    public ReservationResponse(Long id, Long userId, String username, Long bookId, String bookTitle,
                               Long reservedCopyId, String reservedBarcode, LocalDateTime reservedAt,
                               LocalDateTime expiresAt, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservedCopyId = reservedCopyId;
        this.reservedBarcode = reservedBarcode;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public Long getReservedCopyId() { return reservedCopyId; }
    public String getReservedBarcode() { return reservedBarcode; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getStatus() { return status; }

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser() != null ? reservation.getUser().getId() : null,
                reservation.getUser() != null ? reservation.getUser().getUsername() : "Unknown",
                reservation.getBook() != null ? reservation.getBook().getId() : null,
                reservation.getBook() != null ? reservation.getBook().getTitle() : "Unknown",
                reservation.getReservedCopy() != null ? reservation.getReservedCopy().getId() : null,
                reservation.getReservedCopy() != null ? reservation.getReservedCopy().getBarcode() : null,
                reservation.getReservedAt(),
                reservation.getExpiresAt(),
                reservation.getStatus().name());
    }
}
