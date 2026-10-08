package com.libraflow.library.dto.response;

import java.time.LocalDate;

public record BookCopyResponse(
        Long id,
        String barcode,
        String bookTitle,
        String status,
        String shelfLocation,
        LocalDate acquiredAt
) {
}
