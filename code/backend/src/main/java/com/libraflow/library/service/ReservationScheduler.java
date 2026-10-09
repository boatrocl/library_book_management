package com.libraflow.library.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Periodically releases copies whose 48-hour reservation window has elapsed. */
@Component
public class ReservationScheduler {

    private final ReservationService reservationService;

    public ReservationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "${libraflow.reservation.scheduler.cron:0 * * * * ?}")
    public void expireReadyReservations() {
        reservationService.expireReadyReservations(LocalDateTime.now());
    }
}
