package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // หาคิวจองที่เก่าที่สุด (คิวแรก) สำหรับหนังสือเล่มนั้นๆ ที่สถานะเป็น WAITING
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);

    List<Reservation> findAllByBookIdAndStatusOrderByReservedAtAscIdAsc(Long bookId, ReservationStatus status);

    Optional<Reservation> findByUserIdAndReservedCopyIdAndStatusAndExpiresAtAfter(
            Long userId, Long copyId, ReservationStatus status, LocalDateTime now);

    List<Reservation> findAllByStatusAndExpiresAtLessThanEqual(ReservationStatus status, LocalDateTime now);

    Page<Reservation> findAllByStatus(ReservationStatus status, Pageable pageable);

    Page<Reservation> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<ReservationStatus> statuses);
}
