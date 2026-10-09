package com.libraflow.library.service.impl;

import com.libraflow.library.dto.request.ReportRequest;
import com.libraflow.library.pattern.template.ReportFileGenerator;
import com.libraflow.library.service.ReportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportFileGenerator csvGenerator;
    private final ReportFileGenerator pdfGenerator;

    public ReportServiceImpl(
            @Qualifier("csvReportGenerator") ReportFileGenerator csvGenerator,
            @Qualifier("pdfReportGenerator") ReportFileGenerator pdfGenerator
    ) {
        this.csvGenerator = csvGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public byte[] generateLoanReport(LocalDate from, LocalDate to) {
        return csvGenerator.generate(new ReportRequest("LOAN", from, to));
    }

    @Override
    public byte[] generateOverdueReport() {
        return pdfGenerator.generate(new ReportRequest("OVERDUE", null, null));
    }
}
