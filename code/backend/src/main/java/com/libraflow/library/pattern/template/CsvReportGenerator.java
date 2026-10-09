package com.libraflow.library.pattern.template;

import com.libraflow.library.dto.response.ReportRow;
import com.libraflow.library.repository.LoanRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("csvReportGenerator")
public class CsvReportGenerator extends AbstractReportGenerator {

    public CsvReportGenerator(LoanRepository loanRepository, TransactionTemplate transactionTemplate) {
        super(loanRepository, transactionTemplate);
    }

    @Override
    protected byte[] render(List<ReportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Loan Code,Member,Date,Status\n");
        for (ReportRow row : rows) {
            sb.append(row.loanCode()).append(",")
              .append(row.memberName()).append(",")
              .append(row.loanDate()).append(",")
              .append(row.status()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}