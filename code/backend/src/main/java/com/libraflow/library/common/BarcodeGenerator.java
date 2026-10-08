package com.libraflow.library.common;

import org.springframework.stereotype.Component;

/**
 * สร้างบาร์โค้ดตัวเล่มรูปแบบ LIB-00231 ต่อจากเลขล่าสุดที่มีอยู่
 *
 * ออกแบบให้เป็น "ฟังก์ชันบริสุทธิ์" คือรับค่าเข้ามาแล้วคืนค่าออกไป ไม่แตะฐานข้อมูลเอง
 * ทำให้ unit test ได้โดยไม่ต้องยก Spring หรือฐานข้อมูลขึ้นมาเลย
 *
 * ข้อจำกัดที่รู้ตัว: ถ้ามีบรรณารักษ์สองคนกดเพิ่มตัวเล่มพร้อมกันเป๊ะ ๆ อาจได้เลขชนกัน
 * ซึ่งจะถูกดักด้วย UNIQUE constraint บนคอลัมน์ barcode อีกชั้นหนึ่ง
 */
@Component
public class BarcodeGenerator {

    private static final String PREFIX = "LIB-";
    private static final int NUMBER_LENGTH = 5;

    /**
     * @param currentMaxBarcode บาร์โค้ดล่าสุดในระบบ เช่น "LIB-00231" ส่ง null ได้ถ้ายังไม่มีเลย
     * @return บาร์โค้ดถัดไป เช่น "LIB-00232"
     */
    public String next(String currentMaxBarcode) {
        long nextNumber = 1L;

        if (currentMaxBarcode != null && currentMaxBarcode.startsWith(PREFIX)) {
            String digits = currentMaxBarcode.substring(PREFIX.length());
            try {
                nextNumber = Long.parseLong(digits) + 1L;
            } catch (NumberFormatException ex) {
                // บาร์โค้ดที่กรอกมือไว้อาจไม่ใช่ตัวเลขล้วน ถือว่าเริ่มนับใหม่จาก 1
                nextNumber = 1L;
            }
        }

        return PREFIX + String.format("%0" + NUMBER_LENGTH + "d", nextNumber);
    }
}
