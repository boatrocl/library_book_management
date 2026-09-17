package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
