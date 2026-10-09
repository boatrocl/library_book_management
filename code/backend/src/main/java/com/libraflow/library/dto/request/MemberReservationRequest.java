package com.libraflow.library.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;

/** A member's self-service request to join a book's reservation queue. */
public record MemberReservationRequest(
        @NotNull(message = "ต้องระบุรหัสหนังสือ")
        @Positive(message = "รหัสหนังสือต้องเป็นจำนวนเต็มบวก")
        Long bookId,
        @AssertTrue(message = "กรุณายอมรับกฎและเงื่อนไขก่อนจองหนังสือ")
        boolean termsAccepted
) {
}
