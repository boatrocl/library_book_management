package com.libraflow.library.controller.api;

import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.dto.request.BorrowRequest;
import com.libraflow.library.dto.request.MemberBorrowRequest;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller สำหรับงานยืม-คืนหนังสือ (Circulation)
 *
 * สิทธิ์การใช้งาน:
 * - บันทึกการยืม คืน ต่ออายุ และดูรายการทั้งหมด: บรรณารักษ์ (LIBRARIAN / ADMIN)
 * - ดูรายละเอียดใบยืม: บรรณารักษ์ หรือผู้ใช้ที่เป็นเจ้าของใบยืม
 * - ลบ/ยกเลิกใบยืม: ผู้ดูแลระบบ (ADMIN)
 *
 * อ้างอิง:
 * - doc/api-spec.md
 * - doc/diagrams/05-sequence-borrow.puml
 * - doc/diagrams/06-sequence-return.puml
 */
@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Circulation (loans)", description = "ระบบยืม-คืนหนังสือ: ยืม คืน ต่ออายุ และสืบค้นรายการใบยืม")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /** Member borrows one available copy without sending a member id or barcode. */
    @Operation(summary = "สมาชิกยืมหนังสือด้วยตนเอง", description = "ระบุตัวสมาชิกจาก JWT และเลือกตัวเล่มว่างให้โดยอัตโนมัติ จากนั้นตรวจสอบกฎ BR-01..BR-04 และกำหนดวันคืนตาม MemberTier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างรายการยืมสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "bookId ไม่ถูกต้อง"),
            @ApiResponse(responseCode = "404", description = "ไม่พบสมาชิกหรือหนังสือ"),
            @ApiResponse(responseCode = "409", description = "สมาชิกติดเงื่อนไข หรือไม่มีตัวเล่มว่าง")
    })
    @PostMapping("/self")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<LoanResponse> borrowForMember(
            @Valid @RequestBody MemberBorrowRequest request,
            Authentication authentication) {
        LoanResponse response = loanService.borrowForMember(authentication.getName(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/loans/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Staff creates a loan for a member by scanning their id and copy barcodes. */
    @Operation(summary = "บันทึกการยืมที่เคาน์เตอร์", description = "บรรณารักษ์ระบุสมาชิกและบาร์โค้ดตัวเล่ม ระบบตรวจ BorrowRule chain และกำหนดวันคืนตาม MemberTier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "บันทึกการยืมสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ผ่าน validation"),
            @ApiResponse(responseCode = "404", description = "ไม่พบสมาชิกหรือบาร์โค้ดหนังสือ"),
            @ApiResponse(responseCode = "409", description = "สมาชิกติดเงื่อนไข หรือหนังสือไม่ว่าง")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        LoanResponse response = loanService.borrow(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * บันทึกการคืนหนังสือ (PATCH /api/v1/loans/{id}/return)
     * ประมวลผลผ่าน State Pattern และส่งแจ้งเตือนผ่าน Observer Pattern (BR-10)
     */
    @Operation(summary = "บันทึกการคืนหนังสือ", description = "บันทึกคืนหนังสือในใบยืม เปลี่ยนสถานะ BookCopy เป็น AVAILABLE และแจ้งคิวจอง")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "บันทึกการคืนสำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบใบยืม"),
            @ApiResponse(responseCode = "409", description = "สถานะใบยืมไม่ถูกต้อง หรือคืนซ้ำ")
    })
    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    /**
     * ต่ออายุใบยืม (PATCH /api/v1/loans/{id}/renew)
     * ขยายวันครบกำหนดคืนตาม BR-06 (สูงสุด 2 ครั้ง และต้องไม่มีคิวจอง)
     */
    @Operation(summary = "ต่ออายุใบยืม", description = "ขยายเวลาการยืมออกไป 7 วัน โดยต่ออายุได้สูงสุด 2 ครั้งตาม BR-06")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ต่ออายุสำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบใบยืม"),
            @ApiResponse(responseCode = "409", description = "ต่ออายุเกินโควต้า หรือสถานะใบยืมไม่ถูกต้อง")
    })
    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<LoanResponse> renewLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.renewLoan(id));
    }

    /**
     * รายการใบยืมทั้งหมด (GET /api/v1/loans)
     * พร้อมตัวกรองตาม status และ pagination
     */
    @Operation(summary = "รายการใบยืมทั้งหมด", description = "ดึงรายการใบยืมทั้งหมด สามารถกรองตาม status ได้ พร้อม pagination & sorting")
    @ApiResponse(responseCode = "200", description = "สำเร็จ")
    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<PageResponse<LoanResponse>> getAllLoans(
            @RequestParam(required = false) LoanStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loanService.getAllLoans(status, pageable));
    }

    /**
     * ดูรายละเอียดใบยืมตาม id (GET /api/v1/loans/{id})
     */
    @Operation(summary = "ดูรายละเอียดใบยืม", description = "ดึงข้อมูลใบยืมและรายการหนังสือตาม ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบใบยืม")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN') or isAuthenticated()")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    /**
     * ยกเลิก/ลบใบยืม (DELETE /api/v1/loans/{id})
     * อนุญาตเฉพาะ ADMIN สำหรับกรณีบันทึกผิด
     */
    @Operation(summary = "ยกเลิก/ลบใบยืม", description = "ลบใบยืมออกจากระบบ อนุญาตเฉพาะ ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "ลบสำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบใบยืม")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
