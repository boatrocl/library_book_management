package com.libraflow.library.controller.api;

import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.dto.request.CreateReservationRequest;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.dto.response.ReservationResponse;
import com.libraflow.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation", description = "Reservation Management API")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "List reservations", description = "List reservations with optional status filter and pagination")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<PageResponse<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservations(status, pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean staff = hasStaffAccess(authentication);
        ReservationResponse reservation = reservationService.createReservation(
                request.getUserId(), request.getBookId(), authentication.getName(), staff);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        reservationService.cancelReservation(id, authentication.getName(), hasStaffAccess(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean hasStaffAccess(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LIBRARIAN")
                        || authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
