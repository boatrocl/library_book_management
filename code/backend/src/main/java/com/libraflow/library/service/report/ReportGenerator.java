package com.libraflow.library.service.report;

/**
 * Template Method Pattern: คลาสแม่ที่กำหนดโครงสร้างหลักของการสร้างรายงาน
 * (Algorithm Skeleton)
 */
public abstract class ReportGenerator {

    /**
     * Template Method (ห้าม Override)
     * กำหนดลำดับขั้นตอนการทำงานที่ตายตัว
     */
    public final void generateReport() {
        fetchData();
        formatData();
        if (requiresHeader()) {
            addHeader();
        }
        exportToFile();
        System.out.println("Report generation completed.\n");
    }

    // ขั้นตอนที่ 1: ดึงข้อมูล (แต่ละรายงานดึงไม่เหมือนกัน)
    protected abstract void fetchData();

    // ขั้นตอนที่ 2: จัดรูปแบบข้อมูล
    protected abstract void formatData();

    // ขั้นตอนที่ 3: ส่งออกไฟล์
    protected abstract void exportToFile();

    // Hook Method: คลาสลูกสามารถเลือกที่จะ Override หรือไม่ก็ได้
    protected boolean requiresHeader() {
        return true; // ค่าเริ่มต้นคือมี Header
    }

    protected void addHeader() {
        System.out.println("--- LIBRAFLOW SYSTEM REPORT ---");
    }
}
