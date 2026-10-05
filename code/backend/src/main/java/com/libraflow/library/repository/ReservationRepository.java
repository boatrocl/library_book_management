package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // หาคิวจองที่เก่าที่สุด (คิวแรก) สำหรับหนังสือเล่มนั้นๆ ที่สถานะเป็น WAITING
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);
}
