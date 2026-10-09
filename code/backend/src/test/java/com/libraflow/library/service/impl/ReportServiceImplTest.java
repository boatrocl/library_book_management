package com.libraflow.library.service.impl;

import com.libraflow.library.dto.request.ReportRequest;
import com.libraflow.library.pattern.template.ReportFileGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportFileGenerator csvGenerator;

    @Mock
    private ReportFileGenerator pdfGenerator;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(csvGenerator, pdfGenerator);
    }

    @Test
    void generateLoanReport_shouldDelegateDateRangeToCsvGenerator() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 9);
        byte[] csv = "Loan Code,Member,Date,Status\n".getBytes();
        when(csvGenerator.generate(any(ReportRequest.class))).thenReturn(csv);

        assertArrayEquals(csv, reportService.generateLoanReport(from, to));

        ArgumentCaptor<ReportRequest> requestCaptor = ArgumentCaptor.forClass(ReportRequest.class);
        verify(csvGenerator).generate(requestCaptor.capture());
        assertEquals(new ReportRequest("LOAN", from, to), requestCaptor.getValue());
        verifyNoInteractions(pdfGenerator);
    }

    @Test
    void generateOverdueReport_shouldDelegateToPdfGenerator() {
        byte[] pdf = new byte[] { 37, 80, 68, 70 };
        when(pdfGenerator.generate(any(ReportRequest.class))).thenReturn(pdf);

        assertArrayEquals(pdf, reportService.generateOverdueReport());

        ArgumentCaptor<ReportRequest> requestCaptor = ArgumentCaptor.forClass(ReportRequest.class);
        verify(pdfGenerator).generate(requestCaptor.capture());
        assertEquals(new ReportRequest("OVERDUE", null, null), requestCaptor.getValue());
        verifyNoInteractions(csvGenerator);
    }
}
