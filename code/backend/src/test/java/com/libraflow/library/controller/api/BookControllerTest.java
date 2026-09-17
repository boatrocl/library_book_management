package com.libraflow.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.service.BookCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @org.springframework.beans.factory.annotation.Autowired private MockMvc mockMvc;
    // Boot 4 ไม่ลงทะเบียน ObjectMapper ไว้ใน slice ของ WebMvcTest จึงสร้างเองตรงนี้
    // ใช้แค่แปลง request object เป็น JSON string ไม่เกี่ยวกับตัวที่แอปใช้จริงตอนรัน
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private BookCommandService bookCommandService;

    @Test
    @DisplayName("สร้างหนังสือสำเร็จ ต้องได้ 201 พร้อม Location header")
    void shouldCreateBook() throws Exception {
        CreateBookRequest request = new CreateBookRequest("9780132350884", "Clean Code", 2008,
                new BigDecimal("1650.00"), 1L, 1L, List.of(1L));
        when(bookCommandService.create(any())).thenReturn(new BookResponse(
                7L, "9780132350884", "Clean Code", 2008, new BigDecimal("1650.00"),
                "Software Engineering", "Prentice Hall", List.of("Robert C. Martin"), 0L, 0L));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/books/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("ส่ง ISBN ผิดรูปแบบและไม่มีชื่อเรื่อง ต้องได้ 400 พร้อมรายการ fieldErrors")
    void shouldReturnValidationErrors() throws Exception {
        String invalidJson = """
                {"isbn":"123","title":"","publishYear":3000,"categoryId":null,"publisherId":1,"authorIds":[]}
                """;

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/books"));
    }

    @Test
    @DisplayName("สร้างหนังสือด้วย ISBN ซ้ำ ต้องได้ 409 ISBN_ALREADY_EXISTS")
    void shouldReturnConflictOnDuplicateIsbn() throws Exception {
        CreateBookRequest request = new CreateBookRequest("9780132350884", "Clean Code", 2008,
                new BigDecimal("1650.00"), 1L, 1L, List.of(1L));
        when(bookCommandService.create(any()))
                .thenThrow(new BusinessException(ErrorCode.ISBN_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ISBN_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("ลบหนังสือสำเร็จ ต้องได้ 204 และไม่มี body")
    void shouldDeleteBook() throws Exception {
        doNothing().when(bookCommandService).delete(anyLong());

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("BR-11 ลบหนังสือที่ยังมีตัวเล่มถูกยืม ต้องได้ 409 BOOK_IN_USE")
    void shouldReturnConflictWhenBookInUse() throws Exception {
        doThrow(new BusinessException(ErrorCode.BOOK_IN_USE))
                .when(bookCommandService).delete(anyLong());

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("BOOK_IN_USE"));
    }
}
