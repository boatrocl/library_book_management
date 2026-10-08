package com.libraflow.library.service;

import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * งานฝั่ง "อ่าน" ของหนังสือทั้งหมด
 *
 * SOLID - I (Interface Segregation): แยกจาก BookCommandService โดยตั้งใจ
 * เพื่อไม่ให้เกิด Fat Interface ที่รวมทั้งอ่านและเขียนไว้ด้วยกัน
 * ผลที่จับต้องได้คือ PublicCatalogController ซึ่งเปิดให้คนทั่วไปเรียกได้
 * จะขึ้นกับ interface นี้ตัวเดียว และไม่มีทางเรียกเมธอดเขียนข้อมูลได้เลย
 * เพราะไม่มี dependency นั้นอยู่ในคลาส
 *
 * SOLID - D (Dependency Inversion): Controller ขึ้นกับ interface นี้ ไม่ใช่ BookQueryServiceImpl
 */
public interface BookQueryService {

    /** ค้นหาหนังสือพร้อมแบ่งหน้าและเรียงลำดับ (UC02) — keyword และ categoryId เว้นว่างได้ */
    PageResponse<BookResponse> search(String keyword, Long categoryId, Pageable pageable);

    /** ดูรายละเอียดหนังสือรายเล่ม (UC03) — ไม่พบให้โยน ResourceNotFoundException */
    BookResponse findById(Long id);

    /** ตัวเล่มทั้งหมดของหนังสือเล่มหนึ่ง */
    List<BookCopyResponse> findCopies(Long bookId);
}
