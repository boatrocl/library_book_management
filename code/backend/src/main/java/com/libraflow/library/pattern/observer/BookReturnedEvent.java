package com.libraflow.library.pattern.observer;

import org.springframework.context.ApplicationEvent;

/**
 * Domain Event เมื่อมีการคืนตัวเล่มหนังสือสำเร็จ (Observer Pattern)
 * - ฝั่ง Publish: LoanServiceImpl (สมาชิกคนที่ 2) ยิง event เมื่อบันทึกการคืน
 * - ฝั่ง Listen: ReservationNotificationListener & AuditLogListener (สมาชิกคนที่ 3) รับไปแจ้งเตือนคิวจอง BR-10 และบันทึก Log
 *
 * อ้างอิง doc/design-patterns.md ข้อ 3.3, doc/diagrams/04-class-diagram.puml และ doc/diagrams/06-sequence-return.puml
 */
public class BookReturnedEvent extends ApplicationEvent {

    private final Long bookId;
    private final Long copyId;
    private final Long memberId;

    public BookReturnedEvent(Object source, Long bookId, Long copyId, Long memberId) {
        super(source);
        this.bookId = bookId;
        this.copyId = copyId;
        this.memberId = memberId;
    }

    public BookReturnedEvent(Object source, Long bookId, Long copyId) {
        this(source, bookId, copyId, null);
    }

    public BookReturnedEvent(Long bookId, Long copyId, Long memberId) {
        this(BookReturnedEvent.class, bookId, copyId, memberId);
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getCopyId() {
        return copyId;
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public String toString() {
        return "BookReturnedEvent{" +
                "bookId=" + bookId +
                ", copyId=" + copyId +
                ", memberId=" + memberId +
                '}';
    }
}
