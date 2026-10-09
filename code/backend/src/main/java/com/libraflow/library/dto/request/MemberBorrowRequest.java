package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;

/** A member's self-service request to borrow one book title. */
public record MemberBorrowRequest(
        @NotNull(message = "ต้องระบุรหัสหนังสือ")
        @Positive(message = "รหัสหนังสือต้องเป็นจำนวนเต็มบวก")
        Long bookId,
        @AssertTrue(message = "กรุณายอมรับกฎและเงื่อนไขก่อนยืมหนังสือ")
        boolean termsAccepted
) {
}
