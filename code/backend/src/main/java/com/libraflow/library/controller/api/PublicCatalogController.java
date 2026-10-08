package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.BookQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint สาธารณะสำหรับค้นและดูข้อมูลหนังสือ ใครก็เรียกได้โดยไม่ต้องเข้าสู่ระบบ
 *
 * SOLID - I (Interface Segregation) แบบที่จับต้องได้:
 * คลาสนี้ขึ้นกับ BookQueryService ตัวเดียว ไม่รู้จัก BookCommandService เลย
 * ผลคือ **ไม่มีทางที่โค้ดในคลาสนี้จะเรียกเมธอดเขียนข้อมูลได้** ต่อให้เขียนพลาด
 * ก็ compile ไม่ผ่าน เพราะไม่มี dependency นั้นอยู่ในคลาส
 *
 * ถ้ารวมทุกอย่างไว้ใน controller ตัวเดียวที่ถือทั้ง query และ command service
 * การป้องกันแบบนี้จะเหลือแค่ "ความตั้งใจของคนเขียน" ซึ่งพลาดได้เสมอ
 *
 * แยกออกมายังทำให้สมาชิกคนที่ 5 ตั้งค่า Spring Security ได้ง่ายขึ้นด้วย
 * เพราะกฎสิทธิ์แบ่งตามคลาสได้เลย ไม่ต้องไล่ใส่ทีละเมธอด
 */
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Catalog (public)", description = "ค้นหาและดูข้อมูลหนังสือ เปิดให้เข้าถึงได้โดยไม่ต้องล็อกอิน")
public class PublicCatalogController {

    private final BookQueryService bookQueryService;

    public PublicCatalogController(BookQueryService bookQueryService) {
        this.bookQueryService = bookQueryService;
    }

    /**
     * ค้นหาหนังสือพร้อมแบ่งหน้าและเรียงลำดับ (UC02)
     * ตัวอย่าง: /api/v1/books?keyword=clean&categoryId=1&page=0&size=10&sort=title,asc
     *
     * ค้นไม่เจอจะตอบ 200 พร้อม content ว่าง ไม่ใช่ 404 เพราะ "ไม่มีผลลัพธ์" ไม่ใช่ข้อผิดพลาด
     * resource /books มีอยู่จริงเสมอ (UC02 alternative flow 3a)
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

    @Operation(summary = "ดูรายละเอียดหนังสือ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookQueryService.findById(id));
    }

    @Operation(summary = "ตัวเล่มทั้งหมดของหนังสือ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ")
    })
    @GetMapping("/{id}/copies")
    public ResponseEntity<List<BookCopyResponse>> findCopies(@PathVariable Long id) {
        return ResponseEntity.ok(bookQueryService.findCopies(id));
    }
}
