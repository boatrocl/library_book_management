package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.request.UpdateBookRequest;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.BookCommandService;
import com.libraflow.library.service.BookQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
    private final BookCommandService bookCommandService;

    public BookController(BookQueryService bookQueryService, BookCommandService bookCommandService) {
        this.bookQueryService = bookQueryService;
        this.bookCommandService = bookCommandService;
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

    /**
     * เพิ่มหนังสือใหม่ — 201 Created พร้อม Location header ชี้ไปยัง resource ที่เพิ่งสร้าง
     * ตามมาตรฐาน HTTP (ใบงานข้อ 7 กำหนดให้ใช้ status code ถูกต้อง)
     *
     * Valid สั่งให้ Bean Validation ตรวจ request ก่อนเข้าเมธอด ถ้าไม่ผ่าน Spring จะโยน
     * MethodArgumentNotValidException ซึ่ง GlobalExceptionHandler แปลงเป็น 400 พร้อม fieldErrors
     *
     * ISBN ซ้ำ -> 409 ISBN_ALREADY_EXISTS
     */
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        BookResponse created = bookCommandService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** แก้ไขหนังสือทั้งก้อน — 200 OK พร้อมข้อมูลหลังแก้ */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookCommandService.update(id, request));
    }

    /**
     * ลบหนังสือ — 204 No Content เพราะลบสำเร็จแล้วไม่มีอะไรจะส่งกลับ
     * ถ้ายังมีตัวเล่มถูกยืมหรือถูกจอง จะได้ 409 BOOK_IN_USE ตาม BR-11
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
