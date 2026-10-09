package com.libraflow.library.service;

import java.time.LocalDate;

public interface ReportService {

    byte[] generateLoanReport(LocalDate from, LocalDate to);

    byte[] generateOverdueReport();
}
