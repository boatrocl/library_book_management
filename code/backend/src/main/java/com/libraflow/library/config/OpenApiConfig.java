package com.libraflow.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ตั้งค่าเอกสาร OpenAPI / Swagger UI
 *
 * bearerAuth ทำให้ Swagger UI
 * มีปุ่ม Authorize และส่ง
 * Authorization: Bearer <token>
 * ไปกับ request หลังจากใส่ JWT
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraFlowOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()

                                .title(
                                        "LibraFlow API"
                                )

                                .description(
                                        """
                                        REST API ของระบบจัดการหนังสือในห้องสมุด LibraFlow
                                        จัดทำในรายวิชา CP353002 Principles of Software Design and Development
                                        """
                                )

                                .version(
                                        "v1"
                                )

                                .contact(
                                        new Contact()
                                                .name(
                                                        "LibraFlow Team"
                                                )
                                )

                                .license(
                                        new License()
                                                .name(
                                                        "Academic use only"
                                                )
                                )
                )

                .servers(
                        List.of(
                                new Server()
                                        .url(
                                                "http://localhost:8080"
                                        )
                                        .description(
                                                "Local development"
                                        )
                        )
                )

                .components(
                        new Components()

                                .addSecuritySchemes(
                                        "bearerAuth",

                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme(
                                                        "bearer"
                                                )
                                                .bearerFormat(
                                                        "JWT"
                                                )
                                                .description(
                                                        "ใส่ JWT ที่ได้จาก POST /api/v1/auth/login"
                                                )
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(
                                        "bearerAuth"
                                )
                );
    }
}