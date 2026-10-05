package com.libraflow.library.service.report;

import org.springframework.stereotype.Service;

/**
 * Concrete Class 1: รายงานสรุปค่าปรับ
 */
@Service
public class FineReportGenerator extends ReportGenerator {

    @Override
    protected void fetchData() {
        System.out.println("[FineReport] Fetching UNPAID fines from database...");
    }

    @Override
    protected void formatData() {
        System.out.println("[FineReport] Formatting fines data into a financial summary table...");
    }

    @Override
    protected void exportToFile() {
        System.out.println("[FineReport] Exporting fine report as PDF file...");
    }

    // เลิกใช้ Header แบบปกติ และเขียน Header ของตัวเอง (Override Hook Method)
    @Override
    protected void addHeader() {
        System.out.println("--- FINANCIAL FINE REPORT ---");
    }
}
