package com.libraflow.library.pattern.chain;

import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * กฎข้อที่ 1 ของ Chain of Responsibility — BR-01: สมาชิกที่มีสถานะ SUSPENDED ยืมหนังสือไม่ได้
 * อ้างอิง doc/project-overview.md ข้อ 3 และ doc/diagrams/05-sequence-borrow.puml ขั้นที่ 42
 */
@Component
public class MemberStatusRule implements BorrowRule {

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void check(BorrowContext ctx) {
        if (ctx.getMember() == null || !ctx.getMember().isActive()) {
            throw new BusinessException(
                    ErrorCode.MEMBER_SUSPENDED,
                    "บัญชีสมาชิกถูกระงับ ไม่สามารถยืมหนังสือได้"
            );
        }
    }
}
