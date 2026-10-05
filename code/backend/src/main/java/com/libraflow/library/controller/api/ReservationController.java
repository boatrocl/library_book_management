package com.libraflow.library.controller.api;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.dto.request.CreateReservationRequest;
import com.libraflow.library.dto.response.ReservationResponse;
import com.libraflow.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation", description = "Reservation Management API")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        
        Reservation reservation = reservationService.createReservation(request.getUserId(), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    // Mapper method สำหรับแปลง Entity ไปเป็น DTO (ลดความซับซ้อนด้วยการใส่ใน Controller ตรงๆ หรือแยกคลาสก็ได้)
    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getUser() != null ? r.getUser().getUsername() : "Unknown",
                r.getBook() != null ? r.getBook().getId() : null,
                r.getBook() != null ? r.getBook().getTitle() : "Unknown",
                r.getReservedAt(),
                r.getExpiresAt(),
                r.getStatus().name()
        );
    }
}
