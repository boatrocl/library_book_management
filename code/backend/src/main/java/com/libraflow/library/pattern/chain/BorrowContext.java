package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.MemberTier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * บริบทข้อมูลสำหรับตรวจสอบสิทธิ์การยืมหนังสือ
 * รวบรวมข้อมูลที่จำเป็นสำหรับ BorrowRule ทุกข้อใน Chain of Responsibility
 */
public class BorrowContext {

    private final User member;
    private final MemberTier memberTier;
    private final List<BookCopy> copies;
    private final long currentActiveLoanCount;
    private final BigDecimal totalUnpaidFine;
    private final Set<Long> reservationEligibleCopyIds;

    public BorrowContext(User member,
                         MemberTier memberTier,
                         List<BookCopy> copies,
                         long currentActiveLoanCount,
                         BigDecimal totalUnpaidFine) {
        this(member, memberTier, copies, currentActiveLoanCount, totalUnpaidFine, Set.of());
    }

    public BorrowContext(User member,
                         MemberTier memberTier,
                         List<BookCopy> copies,
                         long currentActiveLoanCount,
                         BigDecimal totalUnpaidFine,
                         Set<Long> reservationEligibleCopyIds) {
        this.member = member;
        this.memberTier = memberTier != null ? memberTier : MemberTier.STUDENT;
        this.copies = copies != null ? List.copyOf(copies) : Collections.emptyList();
        this.currentActiveLoanCount = currentActiveLoanCount;
        this.totalUnpaidFine = totalUnpaidFine != null ? totalUnpaidFine : BigDecimal.ZERO;
        this.reservationEligibleCopyIds = reservationEligibleCopyIds == null
                ? Set.of()
                : Set.copyOf(reservationEligibleCopyIds);
    }

    public BorrowContext(User member, List<BookCopy> copies) {
        this(member, MemberTier.STUDENT, copies, 0L, BigDecimal.ZERO);
    }

    public User getMember() {
        return member;
    }

    public MemberTier getMemberTier() {
        return memberTier;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public long getCurrentActiveLoanCount() {
        return currentActiveLoanCount;
    }

    public BigDecimal getTotalUnpaidFine() {
        return totalUnpaidFine;
    }

    public boolean isCopyReservedForMember(Long copyId) {
        return copyId != null && reservationEligibleCopyIds.contains(copyId);
    }

    public int getRequestedCount() {
        return copies.size();
    }
}
