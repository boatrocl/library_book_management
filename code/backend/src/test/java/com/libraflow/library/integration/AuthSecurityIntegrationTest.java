package com.libraflow.library.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration Test ของ Authentication / Authorization
 *
 * Test นี้ใช้ PostgreSQL จริงผ่าน Testcontainers
 * และโหลด Spring ApplicationContext จริงทั้งระบบ
 *
 * Flow ที่ทดสอบ:
 *
 * Testcontainers PostgreSQL
 *          ↓
 * Flyway V1 -> V2 -> V3 -> V3.1
 *          ↓
 * Spring Boot
 *          ↓
 * Login / JWT
 *          ↓
 * Spring Security
 *          ↓
 * Protected API
 */
@Testcontainers
@SpringBootTest(
        properties = {

                /*
                 * Secret สำหรับ integration test เท่านั้น
                 *
                 * เมื่อ decode แล้วมีความยาว 32 bytes
                 * ตรงตาม requirement ของ JwtService
                 */
                "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",

                "app.jwt.expiration-ms=86400000",

                "app.cors.allowed-origin=http://localhost:5173"
        }
)
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    /**
     * Container เป็น static เพื่อให้ PostgreSQL
     * ถูกสร้างเพียงครั้งเดียวต่อ test class
     *
     * ไม่ต้องใช้ PostgreSQL ที่ localhost:5432
     * และไม่กระทบ database สำหรับ development
     */
    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:16"
            )
                    .withDatabaseName(
                            "libraflow_integration_test"
                    )
                    .withUsername(
                            "libraflow_test"
                    )
                    .withPassword(
                            "libraflow_test"
                    );


    /**
     * ส่ง JDBC URL / username / password
     * ของ container แบบ dynamic เข้า Spring Boot
     *
     * Testcontainers จะเลือก host port
     * ให้เอง เช่น 32771 -> 5432
     */
    @DynamicPropertySource
    static void configureDatasource(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
    }


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JdbcTemplate jdbcTemplate;


    private final ObjectMapper objectMapper =
            new ObjectMapper();


    /**
     * ตรวจว่า Flyway สามารถสร้าง database
     * ตั้งแต่ V1 จนถึง migration authentication V3.1 ได้จริง
     */
    @Test
    void flywayShouldApplyAuthenticationMigration() {

        Integer migrationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '3.1'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertNotNull(
                migrationCount
        );

        assertEquals(
                1,
                migrationCount
        );
    }


    /**
     * ตรวจว่า seed accounts จาก V3 / V3.1
     * ถูกสร้างใน PostgreSQL จริง
     */
    @Test
    void authenticationSeedUsersShouldExist() {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM users
                        WHERE username IN (
                            'admin',
                            'librarian01',
                            'member01'
                        )
                        """,
                        Integer.class
                );

        assertNotNull(
                count
        );

        assertEquals(
                3,
                count
        );
    }


    /**
     * GET catalog ถูกกำหนดเป็น public
     * จึงต้องเรียกได้โดยไม่ต้องมี JWT
     */
    @Test
    void publicCatalogShouldBeAccessibleWithoutToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/books"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.content"
                        ).isArray()
                );
    }


    /**
     * ตรวจ flow:
     *
     * seeded admin
     * -> BCrypt password
     * -> AuthService
     * -> JwtService
     * -> JWT response
     */
    @Test
    void adminLoginShouldReturnJwt()
            throws Exception {

        String responseBody =
                mockMvc.perform(
                                post(
                                        "/api/v1/auth/login"
                                )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {
                                                  "username": "admin",
                                                  "password": "Admin@123"
                                                }
                                                """
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andExpect(
                                jsonPath(
                                        "$.tokenType"
                                ).value(
                                        "Bearer"
                                )
                        )
                        .andExpect(
                                jsonPath(
                                        "$.username"
                                ).value(
                                        "admin"
                                )
                        )
                        .andExpect(
                                jsonPath(
                                        "$.role"
                                ).value(
                                        "ADMIN"
                                )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(
                        responseBody
                );

        String token =
                json.get(
                                "token"
                        )
                        .asText();

        assertNotNull(
                token
        );

        assertTrue(
                token.length() > 20
        );
    }


    /**
     * POST /books เป็น protected endpoint
     *
     * ไม่มี JWT
     * -> ต้องถูก Security block ก่อนเข้า Controller
     */
    @Test
    void protectedEndpointShouldRejectRequestWithoutToken()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/books"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }


    /**
     * MEMBER login ได้ตามปกติ
     *
     * แต่ MEMBER ไม่มี ROLE_ADMIN / ROLE_LIBRARIAN
     * จึง POST /books ไม่ได้
     */
    @Test
    void memberShouldReceiveForbiddenOnBookCreation()
            throws Exception {

        String token =
                loginAndGetToken(
                        "member01",
                        "Mem@123"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/books"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void memberCanBorrowAvailableBookThroughSelfServiceEndpoint() throws Exception {
        String token = loginAndGetToken("member01", "Mem@123");
        Long bookId = jdbcTemplate.queryForObject(
                "SELECT book_id FROM book_copies WHERE status = 'AVAILABLE' ORDER BY book_id LIMIT 1",
                Long.class
        );
        assertNotNull(bookId);

        mockMvc.perform(
                        post("/api/v1/loans/self")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"bookId\": " + bookId + ", \"termsAccepted\": true}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].dueDate").isNotEmpty());

        Integer activeCopies = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND status = 'ON_LOAN'",
                Integer.class,
                bookId
        );
        assertNotNull(activeCopies);
        assertTrue(activeCopies > 0);
    }

    @Test
    void librarianCannotUseMemberSelfServiceBorrowEndpoint() throws Exception {
        String token = loginAndGetToken("librarian01", "Lib@123");

        mockMvc.perform(
                        post("/api/v1/loans/self")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"bookId\": 1, \"termsAccepted\": true}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void memberSelfServiceActionsRequireTermsAcceptance() throws Exception {
        String token = loginAndGetToken("member01", "Mem@123");

        mockMvc.perform(post("/api/v1/loans/self")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": 1, \"termsAccepted\": false}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/reservations/self")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": 1, \"termsAccepted\": false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void memberCanReserveUnavailableBookListReservationsAndCancel() throws Exception {
        String token = loginAndGetToken("member01", "Mem@123");
        Long bookId = jdbcTemplate.queryForObject(
                "SELECT book_id FROM book_copies WHERE status = 'AVAILABLE' ORDER BY book_id LIMIT 1",
                Long.class
        );
        assertNotNull(bookId);
        var availableCopyIds = jdbcTemplate.queryForList(
                "SELECT id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE'",
                Long.class,
                bookId
        );
        availableCopyIds.forEach(copyId -> jdbcTemplate.update(
                "UPDATE book_copies SET status = 'ON_LOAN' WHERE id = ?", copyId));

        Long reservationId = null;
        try {
            String response = mockMvc.perform(
                            post("/api/v1/reservations/self")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"bookId\": " + bookId + ", \"termsAccepted\": true}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING"))
                    .andExpect(jsonPath("$.queuePosition").value(1))
                    .andExpect(jsonPath("$.bookId").value(bookId))
                    .andReturn().getResponse().getContentAsString();
            reservationId = objectMapper.readTree(response).get("id").asLong();

            mockMvc.perform(get("/api/v1/reservations/self")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(reservationId));

            mockMvc.perform(delete("/api/v1/reservations/{id}", reservationId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        } finally {
            if (reservationId != null) {
                jdbcTemplate.update("DELETE FROM reservations WHERE id = ?", reservationId);
            }
            availableCopyIds.forEach(copyId -> jdbcTemplate.update(
                    "UPDATE book_copies SET status = 'AVAILABLE' WHERE id = ?", copyId));
        }
    }


    /**
     * ADMIN มีสิทธิ์ POST /books
     *
     * เราจงใจส่ง {} เพื่อไม่สร้างข้อมูลจริง
     *
     * ถ้า JWT และ Authorization ผ่าน
     * request จะเข้า Controller และ Bean Validation
     *
     * ดังนั้นผลที่ต้องการคือ 400 VALIDATION_FAILED
     * ไม่ใช่ 401 หรือ 403
     */
    @Test
    void adminShouldPassSecurityAndReachValidation()
            throws Exception {

        String token =
                loginAndGetToken(
                        "admin",
                        "Admin@123"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/books"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.errorCode"
                        ).value(
                                "VALIDATION_FAILED"
                        )
                );
    }


    /**
     * Helper สำหรับ login จริงผ่าน HTTP layer
     * ไม่ได้เรียก JwtService โดยตรง
     */
    private String loginAndGetToken(
            String username,
            String password
    ) throws Exception {

        String body =
                """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(
                        username,
                        password
                );

        String response =
                mockMvc.perform(
                                post(
                                        "/api/v1/auth/login"
                                )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                body
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(
                        response
                );

        return json
                .get(
                        "token"
                )
                .asText();
    }
}
