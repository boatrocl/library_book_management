package com.libraflow.library.service.report;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.repository.FineRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.List;

@Service
public class FineReportGenerator extends ReportGenerator {

    private final FineRepository fineRepository;
    private List<Fine> fineData;

    public FineReportGenerator(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    @Override
    protected void fetchData() {
        // ดึงข้อมูลค่าปรับทั้งหมดจากฐานข้อมูลจริงๆ
        System.out.println("[FineReport] Fetching fines from database...");
        this.fineData = fineRepository.findAll();
    }

    @Override
    protected void formatData() {
        System.out.println("[FineReport] Formatting data into PDF structure...");
    }

    @Override
    protected void exportToFile() {
        System.out.println("[FineReport] Exporting fine report as PDF...");
        try (FileOutputStream out = new FileOutputStream("fine_report.pdf")) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Financial Fine Report", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            Font headFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Fine ID", headFont)));
            table.addCell(new PdfPCell(new Phrase("Amount", headFont)));
            table.addCell(new PdfPCell(new Phrase("Status", headFont)));

            for (Fine fine : fineData) {
                table.addCell(String.valueOf(fine.getId()));
                table.addCell(String.valueOf(fine.getAmount()));
                table.addCell(fine.getStatus().name());
            }
            document.add(table);

            Paragraph summary = new Paragraph("Total Fines: " + fineData.size());
            summary.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(summary);

            document.close();
            System.out.println("[FineReport] Success! PDF saved to fine_report.pdf");

        } catch (Exception e) {
            System.err.println("Failed to generate PDF: " + e.getMessage());
        }
    }

    @Override
    protected void addHeader() {
        System.out.println("--- FINANCIAL FINE REPORT ---");
    }
}
