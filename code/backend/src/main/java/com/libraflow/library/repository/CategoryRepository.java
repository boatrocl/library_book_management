package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository Pattern (doc/design-patterns.md ข้อ 1)
 *
 * เขียนแค่ interface แล้ว Spring Data JPA สร้าง implementation ให้เป็น proxy ตอน runtime
 * ข้อดีคือ (1) ไม่ต้องเขียน JDBC boilerplate เอง (2) ซ่อนรายละเอียดการเข้าถึงข้อมูล
 * และ (3) mock ใน unit test ได้ง่ายเพราะเป็น interface
 *
 * ชื่อเมธอดคือ query เอง — Spring Data แปลง findByXxx / existsByXxx เป็น SQL ให้อัตโนมัติ
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
