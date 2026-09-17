package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.repository.projection.BookCopyCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    /** ตัวเล่มทั้งหมดของหนังสือเล่มหนึ่ง เรียงตามบาร์โค้ดให้ผลลัพธ์คงที่ */
    List<BookCopy> findByBookIdOrderByBarcodeAsc(Long bookId);

    /**
     * ใช้โดยสมาชิกคนที่ 2 ตอนบันทึกการยืม — ดึงตัวเล่มหลายเล่มจากบาร์โค้ดที่สแกนมาในครั้งเดียว
     * (ดู doc/diagrams/05-sequence-borrow.puml ขั้นที่ 30)
     */
    List<BookCopy> findByBarcodeIn(Collection<String> barcodes);

    Optional<BookCopy> findByBarcode(String barcode);

    /** หาตัวเล่มว่างเล่มแรกของหนังสือ ใช้ index idx_copies_book_status (book_id, status) */
    Optional<BookCopy> findFirstByBookIdAndStatus(Long bookId, BookCopyStatus status);

    /** ใช้เติมค่า availableCopies / totalCopies ใน BookResponse */
    long countByBookId(Long bookId);

    long countByBookIdAndStatus(Long bookId, BookCopyStatus status);

    /**
     * หัวใจของ BR-11 — ห้ามลบหนังสือถ้ายังมีตัวเล่มสถานะ ON_LOAN หรือ RESERVED
     * ใช้ existsBy... แทนการดึงรายการมานับ เพราะฐานข้อมูลหยุดค้นทันทีที่เจอแถวแรก
     */
    boolean existsByBookIdAndStatusIn(Long bookId, Collection<BookCopyStatus> statuses);

    boolean existsByBarcode(String barcode);

    /** บาร์โค้ดล่าสุดในระบบ ใช้ให้ BarcodeGenerator สร้างเลขถัดไป (เรียงแบบสตริงได้เพราะเลขเติมศูนย์หน้า) */
    @Query("SELECT MAX(c.barcode) FROM BookCopy c WHERE c.barcode LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxBarcode(@Param("prefix") String prefix);

    /**
     * นับตัวเล่มทั้งหมดและตัวเล่มที่ว่างของหนังสือหลายเล่มพร้อมกันใน query เดียว
     * ใช้ตอนสร้าง BookResponse ของผลลัพธ์แบบแบ่งหน้า เพื่อเลี่ยง N+1
     */
    @Query("""
            SELECT new com.libraflow.library.repository.projection.BookCopyCount(
                       c.book.id,
                       COUNT(c),
                       SUM(CASE WHEN c.status = com.libraflow.library.domain.enums.BookCopyStatus.AVAILABLE THEN 1L ELSE 0L END))
            FROM BookCopy c
            WHERE c.book.id IN :bookIds
            GROUP BY c.book.id
            """)
    List<BookCopyCount> countCopiesByBookIds(@Param("bookIds") Collection<Long> bookIds);
}
