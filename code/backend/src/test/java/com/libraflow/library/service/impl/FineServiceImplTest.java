package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.repository.FineRepository;
import com.libraflow.library.service.strategy.ExternalFineStrategy;
import com.libraflow.library.service.strategy.FineCalculationStrategy;
import com.libraflow.library.service.strategy.StaffFineStrategy;
import com.libraflow.library.service.strategy.StudentFineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceImplTest {

    @Mock
    private FineRepository fineRepository;

    private FineServiceImpl fineService;

    @BeforeEach
    void setUp() {
        List<FineCalculationStrategy> strategies = List.of(
                new StudentFineStrategy(),
                new StaffFineStrategy(),
                new ExternalFineStrategy()
        );
        fineService = new FineServiceImpl(fineRepository, strategies);
    }

    @Test
    void shouldGenerateFineFromMemberTier() {
        LoanItem loanItem = mock(LoanItem.class);
        Fine savedFine = new Fine(loanItem, new BigDecimal("40.00"), 4);
        when(fineRepository.save(any(Fine.class))).thenReturn(savedFine);

        Fine result = fineService.generateFine(loanItem, 4, MemberTier.EXTERNAL);

        assertNotNull(result);
        assertEquals(new BigDecimal("40.00"), result.getAmount());
        assertEquals(4, result.getOverdueDays());
        verify(fineRepository).save(any(Fine.class));
    }

    @Test
    void nullMemberTierFallsBackToStudent() {
        LoanItem loanItem = mock(LoanItem.class);
        Fine savedFine = new Fine(loanItem, new BigDecimal("9.00"), 3);
        when(fineRepository.save(any(Fine.class))).thenReturn(savedFine);

        Fine result = fineService.generateFine(loanItem, 3, null);

        assertEquals(new BigDecimal("9.00"), result.getAmount());
    }

    @Test
    void lostBookFineUsesReplacementPrice() {
        LoanItem loanItem = mock(LoanItem.class);
        BookCopy copy = mock(BookCopy.class);
        Book book = mock(Book.class);
        when(loanItem.getId()).thenReturn(9L);
        when(loanItem.getBookCopy()).thenReturn(copy);
        when(copy.getBook()).thenReturn(book);
        when(book.getPrice()).thenReturn(new BigDecimal("250.00"));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Fine result = fineService.generateLostBookFine(loanItem, 61);

        assertEquals(new BigDecimal("250.00"), result.getAmount());
        assertEquals(61, result.getOverdueDays());
    }
}
