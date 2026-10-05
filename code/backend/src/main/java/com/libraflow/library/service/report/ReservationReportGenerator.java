package com.libraflow.library.service.report;

import org.springframework.stereotype.Service;

/**
 * Concrete Class 2: รายงานสรุปการจองหนังสือ
 */
@Service
public class ReservationReportGenerator extends ReportGenerator {

    @Override
    protected void fetchData() {
        System.out.println("[ReservationReport] Fetching ALL reservations for this month...");
    }

    @Override
    protected void formatData() {
        System.out.println("[ReservationReport] Formatting data into an Excel spreadsheet format...");
    }

    @Override
    protected void exportToFile() {
        System.out.println("[ReservationReport] Exporting reservation report as CSV file...");
    }

    // ไม่ได้ Override requiresHeader() หรือ addHeader() ดังนั้นจะใช้แบบมาตรฐานของคลาสแม่
}
