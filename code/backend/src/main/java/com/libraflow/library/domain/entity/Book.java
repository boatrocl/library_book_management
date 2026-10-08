package com.libraflow.library.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * หนังสือระดับ Title (หนึ่ง ISBN) — ตัวเล่มจริงบนชั้นอยู่ที่ {@link BookCopy}
 * แมปกับตาราง books ตาม doc/data-dictionary.md ข้อ 6
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Integer ไม่ใช่ int เพราะคอลัมน์ nullable — primitive จะกลายเป็น 0 แทน null */
    @Column(name = "publish_year")
    private Integer publishYear;

    /**
     * BigDecimal ไม่ใช่ double เพราะเป็นข้อมูลเงิน (ใช้คิดค่าชดใช้ตาม BR-08)
     * double เก็บทศนิยมฐานสิบไม่ตรง เช่น 0.1 + 0.2 = 0.30000000000000004
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /** updatable = false กันไม่ให้วันที่สร้างถูกแก้ภายหลังผ่าน UPDATE */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * LAZY เพราะ default ของ @ManyToOne คือ EAGER ซึ่งทำให้ดึงหนังสือ 10 เล่ม
     * แล้วยิง query ตาม category/publisher อีก 20 ครั้ง (N+1)
     * ไม่ใส่ cascade เพราะลบหนังสือต้องไม่ลบหมวดหมู่ทิ้งไปด้วย
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    /**
     * Many-to-Many ผ่านตาราง book_authors โดย Book เป็นฝั่ง owner (ฝั่งที่ถือ @JoinTable)
     * ใช้ Set ไม่ใช่ List เพราะ (1) composite PK ของตารางกลางห้ามซ้ำอยู่แล้ว
     * และ (2) @ManyToMany + List ทำให้ Hibernate ลบทุกแถวแล้ว insert ใหม่ทุกครั้งที่แก้
     */
    /**
     * BatchSize แก้ N+1 ของ collection: แทนที่จะยิง query ดึงผู้แต่งทีละเล่ม
     * Hibernate จะรวบ id ของหนังสือในหน้านั้นแล้วยิงครั้งเดียวด้วย WHERE book_id IN (...)
     * ต่างจาก JOIN FETCH ตรงที่ไม่ทำลายการแบ่งหน้าที่ระดับ SQL
     */
    @BatchSize(size = 50)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new LinkedHashSet<>();

    /**
     * ตัวเล่มทั้งหมดของหนังสือเล่มนี้ mappedBy = "book" แปลว่าฝั่ง BookCopy เป็น owner
     * (เป็นฝ่ายที่ถือคอลัมน์ book_id)
     *
     * cascade มีแค่ PERSIST กับ MERGE — จงใจไม่ใส่ REMOVE เพราะ BR-11 ห้ามลบหนังสือ
     * ที่ยังมีตัวเล่มสถานะ ON_LOAN หรือ RESERVED ถ้าใส่ REMOVE ไว้ การลบหนังสือจะกวาด
     * ตัวเล่มที่ถูกยืมอยู่ทิ้งไปด้วย แล้วประวัติการยืมจะขาด
     */
    @OneToMany(mappedBy = "book", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<BookCopy> copies = new LinkedHashSet<>();

    protected Book() {
        // no-arg constructor สำหรับ JPA เท่านั้น
    }

    public Book(String isbn, String title, Integer publishYear, BigDecimal price,
                Category category, Publisher publisher) {
        this.isbn = isbn;
        this.title = title;
        this.publishYear = publishYear;
        this.price = price;
        this.category = category;
        this.publisher = publisher;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /** คืน view ที่แก้ไม่ได้ กันโค้ดชั้นบนไปแก้ collection ข้าม entity */
    public Set<BookCopy> getCopies() {
        return Collections.unmodifiableSet(copies);
    }

    /** ผูกตัวเล่มเข้ากับหนังสือโดยตั้งค่าทั้งสองฝั่งให้ตรงกัน กัน object ใน memory หลุดจาก DB */
    public void addCopy(BookCopy copy) {
        this.copies.add(copy);
        copy.setBook(this);
    }

    /** คืน view ที่แก้ไม่ได้ กันโค้ดชั้นบนไปแก้ collection ข้าม entity */
    public Set<Author> getAuthors() {
        return Collections.unmodifiableSet(authors);
    }

    public void addAuthor(Author author) {
        this.authors.add(author);
    }

    public void removeAuthor(Author author) {
        this.authors.remove(author);
    }

    public void replaceAuthors(Set<Author> newAuthors) {
        this.authors.clear();
        this.authors.addAll(newAuthors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /** ห้ามใส่ category/publisher/authors เพราะเป็น LAZY — แค่เขียน log ก็จะยิง query เพิ่ม */
    @Override
    public String toString() {
        return "Book{id=" + id + ", isbn='" + isbn + "', title='" + title + "'}";
    }
}
