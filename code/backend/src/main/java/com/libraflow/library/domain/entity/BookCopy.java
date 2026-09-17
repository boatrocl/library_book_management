package com.libraflow.library.domain.entity;

import com.libraflow.library.domain.enums.BookCopyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * ตัวเล่มจริงบนชั้น หนึ่งแถวคือหนังสือหนึ่งเล่มที่จับต้องได้ มีบาร์โค้ดของตัวเอง
 * หนึ่ง {@link Book} (Title) มีได้หลาย BookCopy
 * แมปกับตาราง book_copies ตาม doc/data-dictionary.md ข้อ 8
 */
@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barcode", nullable = false, unique = true, length = 30)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * EnumType.STRING สำคัญมาก — ค่า default ของ JPA คือ ORDINAL ซึ่งเก็บเป็นตัวเลข 0,1,2
     * ถ้าวันหนึ่งมีคนเพิ่มค่า enum แทรกกลาง ข้อมูลเดิมทั้งตารางจะเปลี่ยนความหมายทันที
     * และคอลัมน์ใน DB เป็น VARCHAR(20) อยู่แล้วตาม data dictionary
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookCopyStatus status;

    @Column(name = "shelf_location", length = 30)
    private String shelfLocation;

    @Column(name = "acquired_at", nullable = false)
    private LocalDate acquiredAt;

    protected BookCopy() {
        // no-arg constructor สำหรับ JPA เท่านั้น
    }

    public BookCopy(String barcode, Book book, String shelfLocation, LocalDate acquiredAt) {
        this.barcode = barcode;
        this.book = book;
        this.shelfLocation = shelfLocation;
        this.acquiredAt = acquiredAt;
        this.status = BookCopyStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookCopyStatus getStatus() {
        return status;
    }

    public void setStatus(BookCopyStatus status) {
        this.status = status;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    /** ตัวเล่มจะยืมได้ต่อเมื่ออยู่ในสถานะ AVAILABLE เท่านั้น (BR-04) */
    public boolean isAvailable() {
        return status == BookCopyStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookCopy other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BookCopy{id=" + id + ", barcode='" + barcode + "', status=" + status + "}";
    }
}
