package com.libraflow.library.controller.api;

import com.libraflow.library.dto.request.UpdateUserRoleRequest;
import com.libraflow.library.dto.request.UpdateUserStatusRequest;
import com.libraflow.library.dto.response.UserManagementResponse;
import com.libraflow.library.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "User Management",
        description = "จัดการบัญชีผู้ใช้งานสำหรับ ADMIN"
)
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(
            UserManagementService userManagementService
    ) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "ดูรายชื่อผู้ใช้งานทั้งหมด")
    public ResponseEntity<List<UserManagementResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userManagementService.getAllUsers()
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "เปลี่ยนสถานะบัญชีผู้ใช้")
    public ResponseEntity<UserManagementResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userManagementService.updateStatus(id, request, authentication.getName())
        );
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "เปลี่ยนสิทธิ์ผู้ใช้")
    public ResponseEntity<UserManagementResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userManagementService.updateRole(id, request, authentication.getName())
        );
    }
}
