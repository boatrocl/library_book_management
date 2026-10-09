package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.dto.response.ReservationResponse;
import com.libraflow.library.pattern.observer.BookCopyAvailableEvent;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.LoanRepository;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private ApplicationEventPublisher publisher;

    @Test
    void expireReadyReservation_releasesCopyAndAdvancesQueue() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 8, 12, 0);
        Book book = org.mockito.Mockito.mock(Book.class);
        BookCopy copy = org.mockito.Mockito.mock(BookCopy.class);
        Reservation reservation = new Reservation(org.mockito.Mockito.mock(User.class), book);
        reservation.markAsReady(copy, now.minusMinutes(1));

        when(copy.getStatus()).thenReturn(BookCopyStatus.RESERVED);
        when(copy.getBook()).thenReturn(book);
        when(copy.getId()).thenReturn(24L);
        when(book.getId()).thenReturn(7L);
        when(reservationRepository.findAllByStatusAndExpiresAtLessThanEqual(ReservationStatus.READY, now))
                .thenReturn(List.of(reservation));

        ReservationServiceImpl service = new ReservationServiceImpl(
                reservationRepository, userRepository, bookRepository, bookCopyRepository, loanRepository, publisher);

        int expiredCount = service.expireReadyReservations(now);

        assertEquals(1, expiredCount);
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        verify(bookCopyRepository).save(copy);
        verify(publisher).publishEvent(any(BookCopyAvailableEvent.class));
    }

    @Test
    void createReservation_rejectsDuplicateActiveQueueEntry() {
        User user = org.mockito.Mockito.mock(User.class);
        Book book = org.mockito.Mockito.mock(Book.class);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn("member");
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(true);

        ReservationServiceImpl service = new ReservationServiceImpl(
                reservationRepository, userRepository, bookRepository, bookCopyRepository, loanRepository, publisher);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.createReservation(3L, 7L, "member", false));

        assertEquals(ErrorCode.DUPLICATE_RESERVATION, error.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_rejectsWhenAnAvailableCopyExists() {
        User user = org.mockito.Mockito.mock(User.class);
        Book book = org.mockito.Mockito.mock(Book.class);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn("member");
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(bookCopyRepository.countByBookIdAndStatus(7L, BookCopyStatus.AVAILABLE)).thenReturn(1L);

        ReservationServiceImpl service = new ReservationServiceImpl(
                reservationRepository, userRepository, bookRepository, bookCopyRepository, loanRepository, publisher);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.createReservation(3L, 7L, "member", false));

        assertEquals(ErrorCode.BOOK_COPIES_AVAILABLE, error.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservationForMemberCreatesWaitingReservationFromUsername() {
        User user = org.mockito.Mockito.mock(User.class);
        Book book = org.mockito.Mockito.mock(Book.class);
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(3L);
        when(user.getUsername()).thenReturn("member");
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(bookCopyRepository.countByBookIdAndStatus(7L, BookCopyStatus.AVAILABLE)).thenReturn(0L);
        when(book.getId()).thenReturn(7L);
        when(book.getTitle()).thenReturn("A book");
        when(reservationRepository.findAllByBookIdAndStatusOrderByReservedAtAscIdAsc(7L, ReservationStatus.WAITING))
                .thenReturn(List.of(new Reservation(user, book)));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationServiceImpl service = new ReservationServiceImpl(
                reservationRepository, userRepository, bookRepository, bookCopyRepository, loanRepository, publisher);

        ReservationResponse response = service.createReservationForMember("member", 7L);

        assertEquals("WAITING", response.getStatus());
        assertEquals("member", response.getUsername());
        assertEquals("A book", response.getBookTitle());
        assertEquals(1, response.getQueuePosition());
    }

    @Test
    void cancelReservation_rejectsOtherMembersReservation() {
        User owner = org.mockito.Mockito.mock(User.class);
        when(owner.getUsername()).thenReturn("owner");
        Reservation reservation = new Reservation(owner, org.mockito.Mockito.mock(Book.class));
        when(reservationRepository.findById(9L)).thenReturn(Optional.of(reservation));

        ReservationServiceImpl service = new ReservationServiceImpl(
                reservationRepository, userRepository, bookRepository, bookCopyRepository, loanRepository, publisher);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.cancelReservation(9L, "another-member", false));

        assertEquals(ErrorCode.ACCESS_DENIED, error.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
