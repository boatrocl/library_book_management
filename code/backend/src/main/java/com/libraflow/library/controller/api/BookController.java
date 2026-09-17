package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.BookQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Presentation Layer ของ resource "books"
 *
 * MVC — Controller รับ request แล้วส่งต่อให้ Service ทันที ไม่มี business logic
 * และ **ไม่เรียก Repository ตรง ๆ เด็ดขาด** ตามกฎห้ามข้าม Layer ในใบงานข้อ 3
 *
 * SOLID - D: ขึ้นกับ BookQueryService ซึ่งเป็น interface ไม่ใช่ BookQueryServiceImpl
 */
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookQueryService bookQueryService;

    public BookController(BookQueryService bookQueryService) {
        this.bookQueryService = bookQueryService;
    }

    /**
     * ค้นหาหนังสือพร้อมแบ่งหน้าและเรียงลำดับ (UC02)
     * ตัวอย่าง: /api/v1/books?keyword=clean&categoryId=1&page=0&size=10&sort=title,asc
     *
     * Spring แปลง query parameter page / size / sort เป็น Pageable ให้อัตโนมัติ
     * PageableDefault กำหนดค่าเริ่มต้นเมื่อ client ไม่ได้ส่งมา
     *
     * หมายเหตุสำคัญ: ค้นไม่เจอจะตอบ 200 พร้อม content ว่าง ไม่ใช่ 404
     * เพราะ "ไม่มีผลลัพธ์" ไม่ใช่ข้อผิดพลาด — resource /books มีอยู่จริงเสมอ
     * (ระบุไว้ใน use case UC02 alternative flow 3a)
     */
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.search(keyword, categoryId, pageable));
    }

    /** ดูรายละเอียดหนังสือรายเล่ม (UC03) — ไม่พบตอบ 404 ผ่าน GlobalExceptionHandler */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookQueryService.findById(id));
    }
}
