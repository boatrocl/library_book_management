package com.libraflow.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * เปิดระบบ JPA Auditing — ให้ Spring Data เติมวันเวลาที่สร้างหรือแก้ entity ให้อัตโนมัติ
 * เมื่อ entity ติด @EntityListeners(AuditingEntityListener.class) และมีฟิลด์ @CreatedDate
 * หรือ @LastModifiedDate (ตอนนี้ใช้ที่ Book.createdAt)
 *
 * ทำไมแยกเป็นคลาสของตัวเอง ไม่วาง @EnableJpaAuditing ไว้บน LibraflowApplication:
 * เทสแบบ slice อย่าง @WebMvcTest โหลดคลาสหลักของแอปเสมอ ถ้า annotation นี้อยู่ที่นั่น
 * มันจะพยายามสร้าง JPA infrastructure ทั้งที่เทสไม่มีฐานข้อมูล แล้วล้มด้วย
 * "JPA metamodel must not be empty" แต่ @Configuration แยกต่างหากจะไม่ถูกโหลดใน slice test
 * (SOLID - S: คลาสนี้มีหน้าที่เดียวคือเปิด auditing)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
