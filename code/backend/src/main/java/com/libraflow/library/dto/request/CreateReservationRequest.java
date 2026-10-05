package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateReservationRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Book ID must not be null")
    private Long bookId;

    public CreateReservationRequest() {
    }

    public CreateReservationRequest(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
