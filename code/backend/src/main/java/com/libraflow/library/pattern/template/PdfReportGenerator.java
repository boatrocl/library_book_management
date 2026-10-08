package com.libraflow.library.pattern.template;

import com.libraflow.library.dto.response.ReportRow;
import com.libraflow.library.repository.LoanRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component("pdfReportGenerator")
public class PdfReportGenerator extends AbstractReportGenerator {

    public PdfReportGenerator(LoanRepository loanRepository, TransactionTemplate transactionTemplate) {
        super(loanRepository, transactionTemplate);
    }

    @Override
    protected byte[] render(List<ReportRow> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("LibraFlow - Overdue Loans Report", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font headFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Loan Code", headFont)));
            table.addCell(new PdfPCell(new Phrase("Member", headFont)));
            table.addCell(new PdfPCell(new Phrase("Date", headFont)));
            table.addCell(new PdfPCell(new Phrase("Status", headFont)));

            for (ReportRow row : rows) {
                table.addCell(row.loanCode());
                table.addCell(row.memberName());
                table.addCell(row.loanDate());
                table.addCell(row.status());
            }
            document.add(table);

            Paragraph summary = new Paragraph("Total Overdue Records: " + rows.size());
            summary.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(summary);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("เกิดข้อผิดพลาดในการสร้างไฟล์ PDF", e);
        }
    }
}