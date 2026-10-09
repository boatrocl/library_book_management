package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.dto.response.ReservationResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.pattern.observer.BookCopyAvailableEvent;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.ReservationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ApplicationEventPublisher publisher;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            BookRepository bookRepository,
            BookCopyRepository bookCopyRepository,
            ApplicationEventPublisher publisher) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(
            Long userId, Long bookId, String requesterUsername, boolean allowCreatingForOthers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสมาชิก id: " + userId));

        if (!allowCreatingForOthers
                && (requesterUsername == null || !requesterUsername.equals(user.getUsername()))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "สมาชิกสร้างรายการจองได้เฉพาะบัญชีตนเอง");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบหนังสือ id: " + bookId));

        if (reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, List.of(ReservationStatus.WAITING, ReservationStatus.READY))) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION,
                    ErrorCode.DUPLICATE_RESERVATION.getDefaultMessage());
        }

        Reservation reservation = reservationRepository.save(new Reservation(user, book));
        return ReservationResponse.from(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getReservations(ReservationStatus status, Pageable pageable) {
        Page<Reservation> reservations = status == null
                ? reservationRepository.findAll(pageable)
                : reservationRepository.findAllByStatus(status, pageable);
        return PageResponse.from(reservations, ReservationResponse::from);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, String requesterUsername, boolean allowCancellingForOthers) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบรายการจอง id: " + reservationId));

        if (!allowCancellingForOthers
                && (requesterUsername == null || !requesterUsername.equals(reservation.getUser().getUsername()))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "ยกเลิกได้เฉพาะรายการจองของบัญชีตนเอง");
        }
        if (reservation.getStatus() != ReservationStatus.WAITING
                && reservation.getStatus() != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE,
                    ErrorCode.RESERVATION_NOT_CANCELLABLE.getDefaultMessage());
        }

        releaseCopyIfReady(reservation);
        reservation.cancel();
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public int expireReadyReservations(LocalDateTime now) {
        List<Reservation> expiredReservations = reservationRepository
                .findAllByStatusAndExpiresAtLessThanEqual(ReservationStatus.READY, now);

        for (Reservation reservation : expiredReservations) {
            BookCopy releasedCopy = reservation.getReservedCopy();
            reservation.expire();
            reservationRepository.save(reservation);

            if (releasedCopy != null && releasedCopy.getStatus() == BookCopyStatus.RESERVED) {
                releasedCopy.setStatus(BookCopyStatus.AVAILABLE);
                bookCopyRepository.save(releasedCopy);
                publisher.publishEvent(new BookCopyAvailableEvent(
                        this, releasedCopy.getBook().getId(), releasedCopy.getId()));
            }
        }
        return expiredReservations.size();
    }

    private void releaseCopyIfReady(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.READY) {
            return;
        }

        BookCopy copy = reservation.getReservedCopy();
        if (copy != null && copy.getStatus() == BookCopyStatus.RESERVED) {
            copy.setStatus(BookCopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
            publisher.publishEvent(new BookCopyAvailableEvent(this, copy.getBook().getId(), copy.getId()));
        }
    }
}
