package com.libraflow.library.controller.api;

import com.libraflow.library.dto.response.CategoryResponse;
import com.libraflow.library.security.JwtAuthenticationFilter;
import com.libraflow.library.service.CategoryQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("รายการหมวดหมู่ต้องตอบ 200 พร้อม id และชื่อที่หน้าแคตตาล็อกใช้ได้")
    void shouldReturnCategories() throws Exception {
        when(categoryQueryService.findAll()).thenReturn(
                List.of(
                        new CategoryResponse(1L, "Software Engineering"),
                        new CategoryResponse(2L, "Programming Languages")
                )
        );

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Software Engineering"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Programming Languages"));
    }
}
