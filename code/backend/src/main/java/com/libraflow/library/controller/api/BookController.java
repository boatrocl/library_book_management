package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.CreateBookCopyRequest;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.request.UpdateBookRequest;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.service.BookCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Endpoint สำหรับจัดการข้อมูลหนังสือ ใช้โดยบรรณารักษ์เท่านั้น
 *
 * SOLID - I: คลาสนี้ถือเฉพาะ BookCommandService ส่วน endpoint ที่ใช้อ่านข้อมูล
 * ถูกแยกไปอยู่ที่ PublicCatalogController แล้ว การแบ่งแบบนี้ทำให้ขอบเขตสิทธิ์
 * ตรงกับขอบเขตของคลาสพอดี
 *
 * MVC — ไม่มี business logic ในนี้เลย ทุกเมธอดส่งต่อให้ Service ทันที
 * และไม่เรียก Repository ตรง ๆ ตามกฎห้ามข้าม Layer ในใบงานข้อ 3
 *
 * หมายเหตุสำหรับสมาชิกคนที่ 5: ตอนเพิ่ม Spring Security ให้ใส่
 * @PreAuthorize("hasRole('LIBRARIAN')") ที่ระดับคลาสนี้ได้เลย ครอบทุกเมธอดในครั้งเดียว
 */
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books (librarian)", description = "เพิ่ม แก้ไข ลบหนังสือ และจัดการตัวเล่ม")
public class BookController {

    private final BookCommandService bookCommandService;

    public BookController(BookCommandService bookCommandService) {
        this.bookCommandService = bookCommandService;
    }

    /**
     * เพิ่มหนังสือใหม่ — 201 Created พร้อม Location header ชี้ไป resource ที่เพิ่งสร้าง
     *
     * Valid สั่งให้ Bean Validation ตรวจ request ก่อนเข้าเมธอด ถ้าไม่ผ่าน Spring โยน
     * MethodArgumentNotValidException ซึ่ง GlobalExceptionHandler แปลงเป็น 400 พร้อม fieldErrors
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

    /** แก้ไขหนังสือทั้งก้อน — PUT คือการแทนที่ จึงต้องส่งทุก field มาครบ */
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
     * เพิ่มตัวเล่มใหม่ให้หนังสือเล่มหนึ่ง
     *
     * ใช้ URL ซ้อนแบบ /books/{id}/copies แทน /copies?bookId= เพราะตัวเล่มไม่มีความหมาย
     * ถ้าไม่มีหนังสือต้นสังกัด โครงสร้าง URL จึงควรสื่อความเป็นเจ้าของนี้ออกมา
     */
    @Operation(summary = "เพิ่มตัวเล่มใหม่", description = "เว้น barcode ไว้ให้ระบบสร้างต่อจากเลขล่าสุดได้")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างสำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบหนังสือ"),
            @ApiResponse(responseCode = "409", description = "บาร์โค้ดซ้ำ")
    })
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
