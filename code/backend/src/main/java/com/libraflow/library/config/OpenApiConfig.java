package com.libraflow.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ตั้งค่าเอกสาร OpenAPI / Swagger UI (ใบงานข้อ 7 กำหนดให้เข้าถึง /swagger-ui.html ได้จริง)
 *
 * springdoc จะสแกน @RestController ทุกตัวตอน start แล้วสร้างสเปก JSON ที่ /v3/api-docs
 * ส่วน Swagger UI คือหน้าเว็บที่อ่านสเปกนั้นมาแสดงผลและให้กดทดลองยิง API ได้
 * คลาสนี้เติมข้อมูลส่วนหัวที่ springdoc เดาเองไม่ได้ เช่น ชื่อระบบ เวอร์ชัน และ server
 *
 * securityScheme "bearerAuth" ประกาศไว้ล่วงหน้าให้สมาชิกคนที่ 5 ใช้ตอนทำ JWT
 * เมื่อประกาศแล้ว Swagger UI จะมีปุ่ม Authorize ให้ใส่ token
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LibraFlow API")
                        .description("""
                                REST API ของระบบจัดการหนังสือในห้องสมุด LibraFlow
                                จัดทำในรายวิชา CP353002 Principles of Software Design and Development
                                """)
                        .version("v1")
                        .contact(new Contact().name("LibraFlow Team"))
                        .license(new License().name("Academic use only")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("ใส่ JWT ที่ได้จาก POST /api/v1/auth/login")));
    }
}
