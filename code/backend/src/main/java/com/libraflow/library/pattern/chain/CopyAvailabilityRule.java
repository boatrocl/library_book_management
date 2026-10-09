package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BR-04: ยืมได้เฉพาะตัวเล่ม AVAILABLE หรือ RESERVED ที่กันไว้ให้เจ้าของคิว READY ซึ่งยังไม่หมดเวลา
 * อ้างอิง doc/project-overview.md ข้อ 3 และ doc/diagrams/05-sequence-borrow.puml ขั้นที่ 45
 */
@Component
public class CopyAvailabilityRule implements BorrowRule {

    @Override
    public int order() {
        return 4;
    }

    @Override
    public void check(BorrowContext ctx) {
        List<BookCopy> copies = ctx.getCopies();
        if (copies == null || copies.isEmpty()) {
            return;
        }

        List<String> unavailableCopies = copies.stream()
                .filter(copy -> !copy.isAvailable()
                        && !(copy.getStatus() == BookCopyStatus.RESERVED
                        && ctx.isCopyReservedForMember(copy.getId())))
                .map(copy -> copy.getBarcode() + " (สถานะ: " + copy.getStatus() + ")")
                .toList();

        if (!unavailableCopies.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.COPY_NOT_AVAILABLE,
                    String.format(
                            "ตัวเล่มหนังสือไม่พร้อมให้ยืม: %s — ยืมได้เฉพาะตัวเล่มที่มีสถานะ AVAILABLE เท่านั้น",
                            String.join(", ", unavailableCopies)
                    )
            );
        }
    }
}
