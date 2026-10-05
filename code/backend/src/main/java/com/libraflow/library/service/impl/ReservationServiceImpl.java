package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public Reservation createReservation(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        Reservation reservation = new Reservation(user, book);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.cancel();
        reservationRepository.save(reservation);
    }
}
