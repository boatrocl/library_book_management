package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.CategoryResponse;
import com.libraflow.library.service.CategoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public, read-only category endpoint used to populate catalogue filters. */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories (public)", description = "หมวดหมู่ที่ใช้กรองรายการหนังสือ")
public class PublicCategoryController {

    private final CategoryQueryService categoryQueryService;

    public PublicCategoryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    @Operation(summary = "ดูหมวดหมู่หนังสือ", description = "เรียงตามชื่อหมวดหมู่ ไม่ต้องเข้าสู่ระบบ")
    public ResponseEntity<List<CategoryResponse>> findAll() {
        return ResponseEntity.ok(categoryQueryService.findAll());
    }
}
