package com.libraflow.library.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * ห่อผลลัพธ์แบบแบ่งหน้าให้เป็นรูปแบบของเราเอง ตาม doc/api-spec.md ข้อ 2
 *
 * ทำไมไม่ส่ง Page ของ Spring กลับไปตรง ๆ:
 *   1. โครงสร้าง JSON ของ Page มี field ภายในอย่าง "pageable", "sort", "numberOfElements"
 *      ที่ client ไม่ได้ใช้ และ Spring เตือนเองว่ารูปแบบนี้อาจเปลี่ยนในเวอร์ชันหน้า
 *   2. ผูก API contract ไว้กับ class ของ framework = เปลี่ยน framework ทีหลังแล้ว client พัง
 *
 * @param <T> ชนิดของข้อมูลใน content
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /** แปลง Page ของ entity เป็น PageResponse ของ DTO ด้วยฟังก์ชันแปลงที่ส่งเข้ามา */
    public static <E, T> PageResponse<T> from(Page<E> source, Function<E, T> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast());
    }
}
