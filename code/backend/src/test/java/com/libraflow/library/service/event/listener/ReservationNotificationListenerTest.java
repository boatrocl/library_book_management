package com.libraflow.library.service.event.listener;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.service.event.BookReturnedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationNotificationListenerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationNotificationListener listener;

    @Test
    void shouldNotifyAndMarkReadyWhenBookReturned() {
        // Arrange
        Long bookId = 1L;
        BookReturnedEvent event = new BookReturnedEvent(this, bookId);

        Reservation mockReservation = new Reservation(mock(User.class), mock(Book.class));
        // สถานะเริ่มต้นต้องเป็น WAITING
        assertEquals(ReservationStatus.WAITING, mockReservation.getStatus());

        when(reservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING))
                .thenReturn(Optional.of(mockReservation));

        // Act
        listener.onBookReturned(event);

        // Assert
        assertEquals(ReservationStatus.READY, mockReservation.getStatus());
        verify(reservationRepository, times(1)).save(mockReservation);
    }
}
