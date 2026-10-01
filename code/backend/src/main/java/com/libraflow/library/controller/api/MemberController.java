package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.UpdateMemberProfileRequest;
import com.libraflow.library.dto.response.MemberProfileResponse;
import com.libraflow.library.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "จัดการข้อมูลส่วนตัวและประวัติสมาชิก")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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
}