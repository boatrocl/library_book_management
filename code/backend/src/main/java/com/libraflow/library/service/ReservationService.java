package com.libraflow.library.service;

import com.libraflow.library.domain.entity.Reservation;

public interface ReservationService {
    
    /**
     * สร้างรายการจองหนังสือ
     */
    Reservation createReservation(Long userId, Long bookId);
    
    /**
     * ยกเลิกการจอง
     */
    void cancelReservation(Long reservationId);
}
