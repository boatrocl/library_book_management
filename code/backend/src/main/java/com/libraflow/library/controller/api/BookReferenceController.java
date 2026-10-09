package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.BookReferenceOptionsResponse;
import com.libraflow.library.service.BookReferenceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated lookup endpoint for the librarian/admin book editor. */
@RestController
@RequestMapping("/api/v1/book-references")
@Tag(name = "Book references", description = "Current categories, publishers, and authors for book forms")
public class BookReferenceController {

    private final BookReferenceQueryService bookReferenceQueryService;

    public BookReferenceController(BookReferenceQueryService bookReferenceQueryService) {
        this.bookReferenceQueryService = bookReferenceQueryService;
    }

    @GetMapping
    @Operation(summary = "List current book reference values")
    public ResponseEntity<BookReferenceOptionsResponse> findAll() {
        return ResponseEntity.ok(bookReferenceQueryService.findAll());
    }
}
