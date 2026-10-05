package com.libraflow.library.service.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event Object: สำหรับแจ้งเตือนเมื่อมีการคืนหนังสือ (Observer Pattern)
 */
public class BookReturnedEvent extends ApplicationEvent {

    private final Long bookId;

    public BookReturnedEvent(Object source, Long bookId) {
        super(source);
        this.bookId = bookId;
    }

    public Long getBookId() {
        return bookId;
    }
}
