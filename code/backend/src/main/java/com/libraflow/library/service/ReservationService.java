package com.libraflow.library.service;

import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.dto.response.ReservationResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ReservationService {

    /**
     * สร้างรายการจองหนังสือ
     */
    ReservationResponse createReservation(Long userId, Long bookId, String requesterUsername, boolean allowCreatingForOthers);

    PageResponse<ReservationResponse> getReservations(ReservationStatus status, Pageable pageable);

    /**
     * ยกเลิกการจอง
     */
    void cancelReservation(Long reservationId, String requesterUsername, boolean allowCancellingForOthers);

    /** Expire READY reservations past their pickup deadline and release their copies. */
    int expireReadyReservations(LocalDateTime now);
}
