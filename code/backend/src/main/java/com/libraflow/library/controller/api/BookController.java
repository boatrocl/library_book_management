package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.CreateBookCopyRequest;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.request.UpdateBookRequest;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.BookCommandService;
import com.libraflow.library.service.BookQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

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
@Tag(name = "Books", description = "จัดการหนังสือและตัวเล่ม")
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
    @Operation(summary = "ค้นหาหนังสือ", description = "รองรับคำค้น หมวดหมู่ การแบ่งหน้า และการเรียงลำดับ")
    @ApiResponse(responseCode = "200", description = "สำเร็จ (ไม่พบผลลัพธ์จะได้ content ว่าง ไม่ใช่ 404)")
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.search(keyword, categoryId, pageable));
    }

    /** ดูรายละเอียดหนังสือรายเล่ม (UC03) — ไม่พบตอบ 404 ผ่าน GlobalExceptionHandler */
    @Operation(summary = "ดูรายละเอียดหนังสือ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ")
    })
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
    @Operation(summary = "เพิ่มหนังสือใหม่")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ผ่าน validation"),
            @ApiResponse(responseCode = "409", description = "ISBN ซ้ำ")
    })
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
    @Operation(summary = "แก้ไขหนังสือ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ"),
            @ApiResponse(responseCode = "409", description = "ISBN ชนกับเล่มอื่น")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookCommandService.update(id, request));
    }

    /**
     * ลบหนังสือ — 204 No Content เพราะลบสำเร็จแล้วไม่มีอะไรจะส่งกลับ
     * ถ้ายังมีตัวเล่มถูกยืมหรือถูกจอง จะได้ 409 BOOK_IN_USE ตาม BR-11
     */
    @Operation(summary = "ลบหนังสือ", description = "ลบไม่ได้ถ้ายังมีตัวเล่มถูกยืมหรือถูกจอง (BR-11)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "ลบสำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ"),
            @ApiResponse(responseCode = "409", description = "ยังมีตัวเล่มถูกใช้งานอยู่")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ตัวเล่มทั้งหมดของหนังสือเล่มหนึ่ง
     *
     * ใช้ URL ซ้อนแบบ /books/{id}/copies แทน /copies?bookId= เพราะตัวเล่มไม่มีความหมาย
     * ถ้าไม่มีหนังสือต้นสังกัด โครงสร้าง URL จึงควรสื่อความเป็นเจ้าของนี้ออกมาด้วย
     * (ใบงานข้อ 7 กำหนดให้ตั้งชื่อ endpoint แบบ resource-based)
     */
    @Operation(summary = "ตัวเล่มทั้งหมดของหนังสือ")
    @GetMapping("/{id}/copies")
    public ResponseEntity<List<BookCopyResponse>> findCopies(@PathVariable Long id) {
        return ResponseEntity.ok(bookQueryService.findCopies(id));
    }

    /** เพิ่มตัวเล่มใหม่ — ถ้าไม่ส่ง barcode มา ระบบจะสร้างต่อจากเลขล่าสุดให้ */
    @Operation(summary = "เพิ่มตัวเล่มใหม่", description = "เว้น barcode ไว้ให้ระบบสร้างต่อจากเลขล่าสุดได้")
    @PostMapping("/{id}/copies")
    public ResponseEntity<BookCopyResponse> addCopy(@PathVariable Long id,
                                                    @Valid @RequestBody CreateBookCopyRequest request) {
        BookCopyResponse created = bookCommandService.addCopy(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{copyId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
