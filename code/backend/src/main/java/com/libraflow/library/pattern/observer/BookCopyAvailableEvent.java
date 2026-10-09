package com.libraflow.library.pattern.observer;

import org.springframework.context.ApplicationEvent;

/** Event raised when a physical copy becomes available for the reservation queue. */
public class BookCopyAvailableEvent extends ApplicationEvent {

    private final Long bookId;
    private final Long copyId;

    public BookCopyAvailableEvent(Object source, Long bookId, Long copyId) {
        super(source);
        this.bookId = bookId;
        this.copyId = copyId;
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getCopyId() {
        return copyId;
    }
}
