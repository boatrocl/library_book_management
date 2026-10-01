package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.security.JwtAuthenticationFilter;
import com.libraflow.library.service.BookQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ทดสอบเฉพาะชั้น Web / Controller
 *
 * Security filter ถูกปิดใน test slice นี้
 * เพราะ Security/JWT จะถูกทดสอบแยกต่างหาก
 */
@WebMvcTest(PublicCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookQueryService bookQueryService;

    /*
     * WebMvcTest สามารถโหลด Filter bean ได้
     * จึง mock JWT filter เพื่อไม่ให้ context
     * ต้องสร้าง JWT dependency จริง
     */
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName(
            "ค้นหาหนังสือสำเร็จ ต้องได้ 200 พร้อมโครงสร้าง PageResponse ครบ"
    )
    void shouldReturnPagedBooks()
            throws Exception {

        BookResponse book =
                new BookResponse(
                        1L,
                        "9780132350884",
                        "Clean Code",
                        2008,
                        new BigDecimal("1650.00"),
                        "Software Engineering",
                        "Prentice Hall",
                        List.of(
                                "Robert C. Martin"
                        ),
                        3L,
                        5L
                );

        when(
                bookQueryService.search(
                        any(),
                        any(),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageResponse<>(
                        List.of(book),
                        0,
                        10,
                        1L,
                        1,
                        true,
                        true
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/books?keyword=clean&page=0&size=10&sort=title,asc"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].isbn"
                        ).value(
                                "9780132350884"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].authors[0]"
                        ).value(
                                "Robert C. Martin"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].availableCopies"
                        ).value(3)
                )
                .andExpect(
                        jsonPath("$.page")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalPages")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.first")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.last")
                                .value(true)
                );
    }

    @Test
    @DisplayName(
            "ค้นแล้วไม่พบผลลัพธ์ ต้องได้ 200 พร้อม content ว่าง ไม่ใช่ 404"
    )
    void shouldReturnEmptyContentInsteadOfNotFound()
            throws Exception {

        when(
                bookQueryService.search(
                        any(),
                        any(),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0L,
                        0,
                        true,
                        true
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/books?keyword=ไม่มีเล่มนี้แน่นอน"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.content")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(0)
                );
    }

    @Test
    @DisplayName(
            "ดูหนังสือที่ไม่มีอยู่ ต้องได้ 404 พร้อม errorCode RESOURCE_NOT_FOUND"
    )
    void shouldReturnNotFound()
            throws Exception {

        when(
                bookQueryService.findById(
                        anyLong()
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "หนังสือ",
                        999L
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/books/999"
                        )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "RESOURCE_NOT_FOUND"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/books/999"
                                )
                );
    }

    @Test
    @DisplayName(
            "ส่ง id ที่ไม่ใช่ตัวเลข ต้องได้ 400 ไม่ใช่ 500"
    )
    void shouldReturnBadRequestOnTypeMismatch()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/books/abc"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "VALIDATION_FAILED"
                                )
                );
    }
}