package com.libraflow.library.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * ข้อมูลหนังสือที่ส่งกลับไปให้ client ตามตัวอย่างใน doc/api-spec.md ข้อ 2
 *
 * สังเกตว่าเราส่ง categoryName / publisherName / authors เป็นชื่อ ไม่ใช่ object ทั้งก้อน
 * เพราะหน้าจอรายการหนังสือต้องการแค่ชื่อไปแสดง การส่ง entity ทั้งก้อนจะพ่วง field
 * ที่ไม่ได้ใช้ (เช่น biography ของผู้แต่งซึ่งเป็น TEXT ยาว ๆ) ไปด้วยโดยเปล่าประโยชน์
 */
public record BookResponse(
        Long id,
        String isbn,
        String title,
        Integer publishYear,
        BigDecimal price,
        String categoryName,
        String publisherName,
        List<String> authors,
        long availableCopies,
        long totalCopies
) {
}
