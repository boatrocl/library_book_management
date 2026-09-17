package com.libraflow.library.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BarcodeGenerator ออกแบบให้เป็นฟังก์ชันบริสุทธิ์ จึงทดสอบได้โดยไม่ต้องยก Spring
 * หรือฐานข้อมูลขึ้นมาเลย — เป็นผลพลอยได้ของการไม่ให้คลาสนี้แตะ repository เอง
 */
class BarcodeGeneratorTest {

    private final BarcodeGenerator generator = new BarcodeGenerator();

    @Test
    @DisplayName("ยังไม่มีบาร์โค้ดในระบบ ต้องเริ่มที่ LIB-00001")
    void shouldStartFromOneWhenNoBarcodeExists() {
        assertThat(generator.next(null)).isEqualTo("LIB-00001");
    }

    @Test
    @DisplayName("ต่อเลขจากบาร์โค้ดล่าสุดและเติมศูนย์ให้ครบ 5 หลัก")
    void shouldIncrementFromCurrentMax() {
        assertThat(generator.next("LIB-00231")).isEqualTo("LIB-00232");
        assertThat(generator.next("LIB-00009")).isEqualTo("LIB-00010");
    }

    @Test
    @DisplayName("เลขล้น 5 หลักแล้วยังต่อได้ ไม่ตัดทิ้ง")
    void shouldKeepGoingBeyondFiveDigits() {
        assertThat(generator.next("LIB-99999")).isEqualTo("LIB-100000");
    }

    @Test
    @DisplayName("บาร์โค้ดที่กรอกมือไว้ผิดรูปแบบ ต้องไม่ทำให้ระบบพัง")
    void shouldFallBackWhenBarcodeIsNotNumeric() {
        assertThat(generator.next("LIB-ABCDE")).isEqualTo("LIB-00001");
        assertThat(generator.next("OLD-00012")).isEqualTo("LIB-00001");
    }
}
