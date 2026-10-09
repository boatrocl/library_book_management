package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.repository.LoanRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * BR-09: a member may not borrow a title already on their active loan,
 * or request multiple copies of the same title in one checkout.
 */
@Component
public class NoDuplicateTitleLoanRule implements BorrowRule {

    private final LoanRepository loanRepository;

    public NoDuplicateTitleLoanRule(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public int order() {
        return 4;
    }

    @Override
    public void check(BorrowContext ctx) {
        if (ctx.getMember() == null || ctx.getMember().getId() == null) {
            return;
        }

        Set<Long> requestedBookIds = new HashSet<>();
        for (BookCopy copy : ctx.getCopies()) {
            Long bookId = copy.getBook().getId();
            if (!requestedBookIds.add(bookId)
                    || loanRepository.existsActiveLoanByUserIdAndBookId(ctx.getMember().getId(), bookId)) {
                throw new BusinessException(ErrorCode.BOOK_ALREADY_ON_LOAN);
            }
        }
    }
}
