package com.libraflow.library.service.event.listener;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.pattern.observer.BookCopyAvailableEvent;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.ReservationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Observer: ทำหน้าที่ดักจับ (Listen) ApplicationEvent
 * เมื่อตัวเล่มพร้อม จะกันตัวเล่มให้สมาชิกคนแรกในคิวที่รออยู่
 */
@Component
public class ReservationNotificationListener {

    private final ReservationRepository reservationRepository;
    private final BookCopyRepository bookCopyRepository;

    // Dependency Inversion: Constructor Injection
    public ReservationNotificationListener(
            ReservationRepository reservationRepository,
            BookCopyRepository bookCopyRepository) {
        this.reservationRepository = reservationRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    /**
     * ดักจับ event เมื่อมีตัวเล่มพร้อมสำหรับคิวจอง
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBookCopyAvailable(BookCopyAvailableEvent event) {
        Long bookId = event.getBookId();
        if (event.getCopyId() == null) {
            return;
        }

        BookCopy copy = bookCopyRepository.findById(event.getCopyId()).orElse(null);
        if (copy == null || copy.getStatus() != BookCopyStatus.AVAILABLE) {
            return;
        }

        // 1. หาคนที่จองคิวแรกสุดที่รออยู่ (WAITING)
        Optional<Reservation> firstInQueue = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAscIdAsc(bookId, ReservationStatus.WAITING);

        // 2. ถ้ามีคนจอง ให้เปลี่ยนสถานะเป็น READY และกำหนดวันหมดอายุรับหนังสือ (เช่น 48 ชม.)
        firstInQueue.ifPresent(reservation -> {
            reservation.markAsReady(copy, LocalDateTime.now().plusHours(48));
            copy.setStatus(BookCopyStatus.RESERVED);
            reservationRepository.save(reservation);
            bookCopyRepository.save(copy);

            // TODO: เชื่อมอีเมลหรือ SMS จริง; ตอนนี้บันทึก log เพื่อยืนยันคิวเท่านั้น
            System.out.println("Notification sent to User ID: " + reservation.getUser().getId() +
                               " for Book ID: " + bookId + ". Please pick up within 48 hours.");
        });
    }
}
