package com.libraflow.library.controller.api;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.dto.response.FineResponse;
import com.libraflow.library.service.FineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fines")
@Tag(name = "Fine", description = "Fine Management API")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Pay a fine by ID")
    public ResponseEntity<FineResponse> payFine(@PathVariable Long id) {
        Fine updatedFine = fineService.payFine(id);
        return ResponseEntity.ok(toResponse(updatedFine));
    }

    private FineResponse toResponse(Fine f) {
        return new FineResponse(
                f.getId(),
                f.getLoanItem() != null ? f.getLoanItem().getId() : null,
                f.getAmount(),
                f.getOverdueDays(),
                f.getStatus().name(),
                f.getCreatedAt(),
                f.getPaidAt()
        );
    }
}
