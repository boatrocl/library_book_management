package com.libraflow.library.controller.api;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.FineStatus;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.UpdateMemberProfileRequest;
import com.libraflow.library.dto.response.MemberProfileResponse;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.FineResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.MemberService;
import com.libraflow.library.service.LoanService;
import com.libraflow.library.service.FineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "จัดการข้อมูลส่วนตัวและประวัติสมาชิก")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final FineService fineService;
    private final UserRepository userRepository; // นำเข้า UserRepository เพื่อให้แอดมินแก้ไขข้อมูล User ได้

    // สร้าง DTO แบบ Record ไว้รับค่าตอน Admin อัปเดตสิทธิ์และสถานะ
    public record UpdateRoleRequest(UserRole role) {}
    public record UpdateStatusRequest(Boolean isActive) {}

    public MemberController(MemberService memberService, LoanService loanService, FineService fineService, UserRepository userRepository) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.fineService = fineService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "ดึงข้อมูลโปรไฟล์ของสมาชิก")
    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    @Operation(summary = "แก้ไขข้อมูลโปรไฟล์ของสมาชิก")
    @PutMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberProfileRequest request) {
        return ResponseEntity.ok(memberService.updateProfile(id, request));
    }

    @Operation(summary = "ดึงประวัติการยืมของสมาชิก")
    @GetMapping("/{id}/loans")
    public ResponseEntity<PageResponse<LoanResponse>> getMemberLoans(
            @PathVariable Long id,
            @PageableDefault(sort = "loanDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loanService.getMemberLoans(id, pageable));
    }

    @Operation(summary = "ดึงประวัติค่าปรับของสมาชิก")
    @GetMapping("/{id}/fines")
    public ResponseEntity<List<FineResponse>> getMemberFines(
            @PathVariable Long id,
            @RequestParam(required = false) FineStatus status) {
        List<FineResponse> fines = fineService.getFinesByMemberId(id);
        
        if (status != null) {
            fines = fines.stream()
                    .filter(f -> f.getStatus().equals(status.name()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(fines);
    }

    @Operation(summary = "เปลี่ยนสิทธิ์ผู้ใช้งาน (Admin เท่านั้น)")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        // ดึงข้อมูล Username ของแอดมินคนที่กำลังกดยืนยันคำสั่ง
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบผู้ใช้งาน"));

        // ระบบป้องกัน: ถ้า Username ของคนล็อกอิน ตรงกับคนที่กำลังจะถูกเปลี่ยนสิทธิ์ ให้ปฏิเสธคำสั่งทันที
        if (targetUser.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ไม่อนุญาตให้ผู้ดูแลระบบปรับลดสิทธิ์ของตนเอง");
        }

        targetUser.setRole(request.role());
        userRepository.save(targetUser);
        
        return ResponseEntity.ok(Map.of("message", "อัปเดตสิทธิ์สำเร็จ"));
    }

    @Operation(summary = "ระงับ/เปิดใช้งานบัญชี (Admin เท่านั้น)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        // ดึงข้อมูล Username ของแอดมินคนที่กำลังกดยืนยันคำสั่ง
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบผู้ใช้งาน"));

        // ระบบป้องกัน: ห้าม Admin สั่งปิดบัญชี (Suspend) ตัวเอง
        if (targetUser.getUsername().equals(currentUsername) && !request.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ไม่อนุญาตให้ผู้ดูแลระบบระงับบัญชีของตนเอง");
        }

        targetUser.setActive(request.isActive()); // หมายเหตุ: หาก User.java ใช้คำสั่ง setActive() ให้แก้บรรทัดนี้ตามคลาสจริง
        userRepository.save(targetUser);
        
        return ResponseEntity.ok(Map.of("message", "อัปเดตสถานะสำเร็จ"));
    }
}