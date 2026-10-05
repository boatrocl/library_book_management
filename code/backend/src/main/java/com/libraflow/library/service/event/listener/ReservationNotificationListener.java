package com.libraflow.library.service.event.listener;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.service.event.BookReturnedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Observer: ทำหน้าที่ดักจับ (Listen) ApplicationEvent
 * เมื่อหนังสือถูกคืน จะทำการแจ้งเตือนคนที่จองคิวแรกสุด
 */
@Component
public class ReservationNotificationListener {

    private final ReservationRepository reservationRepository;

    // Dependency Inversion: Constructor Injection
    public ReservationNotificationListener(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * ดักจับ Event เมื่อมีคนคืนหนังสือ
     */
    @EventListener
    @Transactional
    public void onBookReturned(BookReturnedEvent event) {
        Long bookId = event.getBookId();

        // 1. หาคนที่จองคิวแรกสุดที่รออยู่ (WAITING)
        Optional<Reservation> firstInQueue = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING);

        // 2. ถ้ามีคนจอง ให้เปลี่ยนสถานะเป็น READY และกำหนดวันหมดอายุรับหนังสือ (เช่น 48 ชม.)
        firstInQueue.ifPresent(reservation -> {
            reservation.markAsReady(LocalDateTime.now().plusHours(48));
            reservationRepository.save(reservation);

            // TODO: ส่งอีเมลหรือข้อความแจ้งเตือนผู้ใช้ว่ามารับหนังสือได้แล้ว (จำลองด้วยการพิมพ์ Log)
            System.out.println("Notification sent to User ID: " + reservation.getUser().getId() +
                               " for Book ID: " + bookId + ". Please pick up within 48 hours.");
        });
    }
}
