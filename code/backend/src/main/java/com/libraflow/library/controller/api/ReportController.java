package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.ReportRequest;
import com.libraflow.library.pattern.template.AbstractReportGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "ระบบออกรายงานด้วย Template Method Pattern")
public class ReportController {

    private final AbstractReportGenerator csvGenerator;
    private final AbstractReportGenerator pdfGenerator;

    public ReportController(
            @Qualifier("csvReportGenerator") AbstractReportGenerator csvGenerator,
            @Qualifier("pdfReportGenerator") AbstractReportGenerator pdfGenerator) {
        this.csvGenerator = csvGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    @Operation(summary = "รายงานสถิติการยืม (CSV)")
    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<byte[]> getLoanReport(
            @RequestParam String from,
            @RequestParam String to) {
        
        ReportRequest req = new ReportRequest("LOAN", LocalDate.parse(from), LocalDate.parse(to));
        byte[] data = csvGenerator.generate(req);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @Operation(summary = "รายงานหนังสือค้างส่ง (PDF)")
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<byte[]> getOverdueReport() {
        
        ReportRequest req = new ReportRequest("OVERDUE", null, null);
        byte[] data = pdfGenerator.generate(req);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=overdue_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}