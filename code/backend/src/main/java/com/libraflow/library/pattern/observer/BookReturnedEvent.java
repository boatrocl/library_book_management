package com.libraflow.library.pattern.observer;

/**
 * Event เมื่อมีการคืนตัวเล่มสำเร็จ โดยคงรหัสสมาชิกผู้คืนไว้เพื่อการติดตาม
 *
 * อ้างอิง doc/design-patterns.md ข้อ 3.3, doc/diagrams/04-class-diagram.puml และ doc/diagrams/06-sequence-return.puml
 */
public class BookReturnedEvent extends BookCopyAvailableEvent {

    private final Long memberId;

    public BookReturnedEvent(Object source, Long bookId, Long copyId, Long memberId) {
        super(source, bookId, copyId);
        this.memberId = memberId;
    }

    public BookReturnedEvent(Object source, Long bookId, Long copyId) {
        this(source, bookId, copyId, null);
    }

    public BookReturnedEvent(Long bookId, Long copyId, Long memberId) {
        this(BookReturnedEvent.class, bookId, copyId, memberId);
    }

    public Long getMemberId() {
        return memberId;
    }
}
