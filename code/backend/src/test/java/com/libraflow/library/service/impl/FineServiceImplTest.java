package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.repository.FineRepository;
import com.libraflow.library.service.strategy.FineCalculationStrategy;
import com.libraflow.library.service.strategy.StandardMemberFineStrategy;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceImplTest {

    @Mock
    private FineRepository fineRepository;

    private FineServiceImpl fineService;

    @BeforeEach
    void setUp() {
        List<FineCalculationStrategy> strategies =
                List.of(new StandardMemberFineStrategy());

        fineService = new FineServiceImpl(fineRepository, strategies);
    }

    @Test
    void shouldGenerateFineCorrectly() {
        // Arrange
        LoanItem mockLoanItem = mock(LoanItem.class);

        int overdueDays = 5;
        String role = "MEMBER";

        Fine mockSavedFine =
                new Fine(mockLoanItem, new BigDecimal("25.00"), overdueDays);

        when(fineRepository.save(any(Fine.class)))
                .thenReturn(mockSavedFine);

        // Act
        Fine result =
                fineService.generateFine(mockLoanItem, overdueDays, role);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("25.00"), result.getAmount());
        assertEquals(5, result.getOverdueDays());

        verify(fineRepository, times(1))
                .save(any(Fine.class));
    }
}