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
     * ค้นหาหนังสือตาม UC02 — รองรับชื่อเรื่อง ISBN ผู้แต่ง และหมวดหมู่
     *
     * BookQueryService แปลงคำค้นว่างเป็น empty string เพื่อให้ query เดียวรองรับ
     * ทั้งกรณีไม่ค้น ค้นคำ และกรองหมวดพร้อมคำค้น โดยไม่ต้องแยกหลายเมธอด
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
            WHERE (:keyword = ''
                   OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR EXISTS (
                       SELECT a.id FROM Book authorBook JOIN authorBook.authors a
                       WHERE authorBook.id = b.id
                         AND LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   ))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (
                   :availability = 'ALL'
                   OR (:availability = 'AVAILABLE'
                       AND EXISTS (
                           SELECT c.id FROM BookCopy c
                           WHERE c.book = b
                             AND c.status = com.libraflow.library.domain.enums.BookCopyStatus.AVAILABLE
                       ))
                   OR (:availability = 'UNAVAILABLE'
                       AND NOT EXISTS (
                           SELECT c.id FROM BookCopy c
                           WHERE c.book = b
                             AND c.status = com.libraflow.library.domain.enums.BookCopyStatus.AVAILABLE
                       ))
              )
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      @Param("availability") String availability,
                      Pageable pageable);

    /** ใช้ตรวจ ISBN ซ้ำก่อนสร้างหนังสือใหม่ เพื่อตอบ 409 ISBN_ALREADY_EXISTS */
    boolean existsByIsbn(String isbn);

    /** ใช้ตอนแก้ไข — ต้องเช็คว่า ISBN ใหม่ไม่ไปชนของเล่มอื่นที่ไม่ใช่ตัวเอง */
    Optional<Book> findByIsbn(String isbn);
}
