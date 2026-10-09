package com.libraflow.library.mapper;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.dto.response.LoanItemResponse;
import com.libraflow.library.dto.response.LoanResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * แปลงข้อมูลระหว่าง Entity Loan / LoanItem กับ Response DTO
 *
 * SOLID - S (Single Responsibility): คลาสนี้ทำหน้าที่แปลงรูปข้อมูลเท่านั้น
 * ไม่มี business logic และไม่เรียก Repository ตรง ๆ เพื่อป้องกันปัญหา N+1
 */
@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        return toResponse(loan, null, null);
    }

    public LoanResponse toResponse(Loan loan, String memberName, String memberTier) {
        String name = (memberName != null && !memberName.isBlank())
                ? memberName
                : loan.getUser().getUsername();

        String tier = (memberTier != null && !memberTier.isBlank())
                ? memberTier
                : "STUDENT";

        List<LoanItemResponse> itemResponses = loan.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new LoanResponse(
                loan.getId(),
                loan.getLoanCode(),
                name,
                tier,
                loan.getLoanDate(),
                loan.getStatus(),
                itemResponses
        );
    }

    public LoanItemResponse toItemResponse(LoanItem item) {
        return new LoanItemResponse(
                item.getId(),
                item.getBookCopy().getBook().getId(),
                item.getBookCopy().getBarcode(),
                item.getBookCopy().getBook().getTitle(),
                item.getDueDate(),
                item.getReturnedAt()
        );
    }

    public List<LoanResponse> toResponses(List<Loan> loans) {
        return loans.stream().map(this::toResponse).toList();
    }
}
