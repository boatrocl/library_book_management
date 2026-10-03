package com.libraflow.library.dto.response;

import java.time.LocalDate;

/**
 * ข้อมูลรายการตัวเล่มในใบยืมที่ส่งกลับให้ client
 * อ้างอิง doc/api-spec.md ข้อ 2
 */
public record LoanItemResponse(
        Long id,
        String barcode,
        String bookTitle,
        LocalDate dueDate,
        LocalDate returnedAt
) {
}
