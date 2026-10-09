package com.libraflow.library.pattern.template;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.dto.request.ReportRequest;
import com.libraflow.library.dto.response.ReportRow;
import com.libraflow.library.repository.LoanRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractReportGenerator implements ReportFileGenerator {

    protected final LoanRepository loanRepository;
    private final TransactionTemplate transactionTemplate;

    protected AbstractReportGenerator(LoanRepository loanRepository, TransactionTemplate transactionTemplate) {
        this.loanRepository = loanRepository;
        this.transactionTemplate = transactionTemplate;
    }

    // นำ final กลับมาเพื่อล็อกโครงสร้าง Template Method ไว้ตามเดิม
    @Override
    public final byte[] generate(ReportRequest req) {
        
        // จำกัดขอบเขตเปิด/ปิด Database Connection ให้อยู่เฉพาะช่วงดึงและแปลงข้อมูล (Lazy Load ทำงานได้อย่างปลอดภัยในบล็อกนี้)
        List<ReportRow> rows = transactionTemplate.execute(status -> {
            List<Loan> rawData = fetchData(req);
            return transform(rawData);
        });

        // เมื่อรันโค้ดถึงบรรทัดนี้ Database Connection จะถูกตัดทิ้งแล้ว ลดปัญหาคอขวดขณะสร้างไฟล์
        return render(rows);
    }

    private List<Loan> fetchData(ReportRequest req) {
        if ("OVERDUE".equalsIgnoreCase(req.type())) {
            return loanRepository.findActiveLoansWithOverdueItems(LocalDate.now());
        } else {
            return loanRepository.findAll().stream()
                    .filter(l -> !l.getLoanDate().toLocalDate().isBefore(req.from()) 
                              && !l.getLoanDate().toLocalDate().isAfter(req.to()))
                    .collect(Collectors.toList());
        }
    }

    private List<ReportRow> transform(List<Loan> rawData) {
        return rawData.stream().map(loan -> new ReportRow(
                loan.getLoanCode(),
                loan.getUser().getUsername(),
                loan.getLoanDate().toLocalDate().toString(),
                loan.getStatus().name()
        )).collect(Collectors.toList());
    }

    protected abstract byte[] render(List<ReportRow> rows);
}
