package com.libraflow.library.dto.response;

import com.libraflow.library.domain.enums.LoanStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ข้อมูลใบยืมที่ส่งกลับให้ client
 * อ้างอิง doc/api-spec.md ข้อ 2
 */
public record LoanResponse(
        Long id,
        String loanCode,
        String memberName,
        String memberTier,
        LocalDateTime loanDate,
        LoanStatus status,
        List<LoanItemResponse> items
) {
}
