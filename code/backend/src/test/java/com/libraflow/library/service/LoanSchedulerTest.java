package com.libraflow.library.service;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSchedulerTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FineService fineService;

    @Mock
    private BookCopy bookCopy;

    @Test
    void lostLoanCreatesReplacementCostFineAndMarksCopyLost() {
        LocalDate today = LocalDate.of(2026, 10, 8);
        User member = new User("member", "hash", "member@example.test", UserRole.MEMBER, true, "STUDENT");
        Loan loan = new Loan("LN-LOST", member, null, LocalDateTime.now(), LoanStatus.OVERDUE);
        LoanItem item = new LoanItem(loan, bookCopy, today.minusDays(61));
        loan.addItem(item);
        when(loanRepository.findOverdueLoansExceedingDays(today.minusDays(60))).thenReturn(List.of(loan));
        when(bookCopy.getStatus()).thenReturn(BookCopyStatus.ON_LOAN);

        LoanScheduler scheduler = new LoanScheduler(loanRepository, fineService);
        int updatedCount = scheduler.detectAndProcessLostLoans(today);

        org.junit.jupiter.api.Assertions.assertEquals(1, updatedCount);
        org.junit.jupiter.api.Assertions.assertEquals(LoanStatus.LOST, loan.getStatus());
        verify(fineService).generateLostBookFine(item, 61);
        verify(bookCopy).setStatus(BookCopyStatus.LOST);
        verify(loanRepository).saveAll(List.of(loan));
    }
}
