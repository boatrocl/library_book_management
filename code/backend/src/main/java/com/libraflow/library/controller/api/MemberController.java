package com.libraflow.library.controller.api;

import com.libraflow.library.domain.enums.FineStatus;
import com.libraflow.library.dto.request.UpdateMemberProfileRequest;
import com.libraflow.library.dto.response.MemberProfileResponse;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.FineResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.service.MemberService;
import com.libraflow.library.service.LoanService;
import com.libraflow.library.service.FineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "จัดการข้อมูลส่วนตัวและประวัติสมาชิก")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final FineService fineService;

    public MemberController(MemberService memberService, LoanService loanService, FineService fineService) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.fineService = fineService;
    }

    @Operation(summary = "ดึงข้อมูลโปรไฟล์ของสมาชิก")
    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> getProfile(@PathVariable Long id, Authentication authentication) {
        authorizeMemberAccess(id, authentication);
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    @Operation(summary = "แก้ไขข้อมูลโปรไฟล์ของสมาชิก")
    @PutMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberProfileRequest request,
            Authentication authentication) {
        authorizeMemberAccess(id, authentication);
        return ResponseEntity.ok(memberService.updateProfile(id, request));
    }

    @Operation(summary = "ดึงประวัติการยืมของสมาชิก")
    @GetMapping("/{id}/loans")
    public ResponseEntity<PageResponse<LoanResponse>> getMemberLoans(
            @PathVariable Long id,
            @PageableDefault(sort = "loanDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        authorizeMemberAccess(id, authentication);
        return ResponseEntity.ok(loanService.getMemberLoans(id, pageable));
    }

    @Operation(summary = "ดึงประวัติค่าปรับของสมาชิก")
    @GetMapping("/{id}/fines")
    public ResponseEntity<List<FineResponse>> getMemberFines(
            @PathVariable Long id,
            @RequestParam(required = false) FineStatus status,
            Authentication authentication) {
        authorizeMemberAccess(id, authentication);
        List<FineResponse> fines = fineService.getFinesByMemberId(id);

        if (status != null) {
            fines = fines.stream()
                    .filter(f -> f.getStatus().equals(status.name()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(fines);
    }

    private void authorizeMemberAccess(Long memberId, Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_LIBRARIAN"));
        memberService.assertCanAccessMember(memberId, authentication.getName(), staff);
    }

}
