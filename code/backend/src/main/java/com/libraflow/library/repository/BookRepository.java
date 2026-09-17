package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * ค้นหาหนังสือตาม UC02 — รองรับทั้งคำค้นและหมวดหมู่ และเว้นว่างได้ทั้งคู่
     *
     * เงื่อนไข ":keyword IS NULL OR ..." ทำให้พารามิเตอร์ที่เป็น null หมายถึง
     * "ไม่กรองด้วยเงื่อนไขนี้" จึงใช้ query เดียวครอบทั้ง 4 กรณี
     * (ไม่กรองเลย / กรองคำค้น / กรองหมวด / กรองทั้งสอง) แทนการเขียนแยกหลายเมธอด
     *
     * Spring Data จะยิงสอง query ให้อัตโนมัติเมื่อ return เป็น Page คือ
     * query ดึงข้อมูลพร้อม LIMIT/OFFSET และ countQuery นับจำนวนทั้งหมด
     * ตามที่เขียนไว้ใน doc/diagrams/07-sequence-search.puml
     */
    /**
     * EntityGraph สั่งให้โหลด category กับ publisher มาพร้อมกันด้วย LEFT JOIN ใน query เดียว
     *
     * ทำไมใส่แค่สองตัวนี้ ไม่ใส่ authors ด้วย:
     * authors เป็น collection ถ้า JOIN FETCH collection พร้อมกับ Pageable
     * Hibernate จะเลิกแบ่งหน้าที่ระดับ SQL แล้วดึงทุกแถวมาแบ่งหน้าในหน่วยความจำแทน
     * (คำเตือน HHH000104 firstResult/maxResults specified with collection fetch)
     * ซึ่งอันตรายมากเมื่อข้อมูลเยอะ — authors จึงแก้ด้วย BatchSize ที่ฝั่ง entity แทน
     */
    @EntityGraph(attributePaths = {"category", "publisher"})
    @Query("""
            SELECT b FROM Book b
            WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      Pageable pageable);

    /** ใช้ตรวจ ISBN ซ้ำก่อนสร้างหนังสือใหม่ เพื่อตอบ 409 ISBN_ALREADY_EXISTS */
    boolean existsByIsbn(String isbn);

    /** ใช้ตอนแก้ไข — ต้องเช็คว่า ISBN ใหม่ไม่ไปชนของเล่มอื่นที่ไม่ใช่ตัวเอง */
    Optional<Book> findByIsbn(String isbn);
}
