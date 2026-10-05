package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.repository.FineRepository;
import com.libraflow.library.service.strategy.FineCalculationStrategy;
import com.libraflow.library.service.strategy.StandardMemberFineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceImplTest {

    @Mock
    private FineRepository fineRepository;

    private FineServiceImpl fineService;

    @BeforeEach
    void setUp() {
        // ใช้ StandardMemberFineStrategy ของจริงเพื่อร่วมทดสอบ
        List<FineCalculationStrategy> strategies = List.of(new StandardMemberFineStrategy());
        fineService = new FineServiceImpl(fineRepository, strategies);
    }

    @Test
    void shouldGenerateFineCorrectly() {
        // Arrange
        LoanItem mockLoanItem = new LoanItem();
        int overdueDays = 5;
        String role = "MEMBER";
        
        Fine mockSavedFine = new Fine(mockLoanItem, new BigDecimal("25.00"), overdueDays);
        when(fineRepository.save(any(Fine.class))).thenReturn(mockSavedFine);

        // Act
        Fine result = fineService.generateFine(mockLoanItem, overdueDays, role);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("25.00"), result.getAmount());
        assertEquals(5, result.getOverdueDays());
        
        // ยืนยันว่ามีการเรียก save() ลงฐานข้อมูล 1 ครั้ง
        verify(fineRepository, times(1)).save(any(Fine.class));
    }
}
