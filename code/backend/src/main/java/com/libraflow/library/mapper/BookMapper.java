package com.libraflow.library.mapper;

import com.libraflow.library.domain.entity.Author;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * แปลงข้อมูลระหว่าง Entity กับ DTO
 *
 * SOLID - S (Single Responsibility): คลาสนี้ทำหน้าที่เดียวคือแปลงรูปข้อมูล
 * ไม่มี business logic และ **ไม่แตะ Repository เลย** จำนวนตัวเล่มจึงรับเข้ามาเป็นพารามิเตอร์
 * ไม่ใช่ไปนับเอง — ถ้า mapper ยิง query ได้เมื่อไหร่ มันจะกลายเป็นแหล่งกำเนิด N+1 ทันที
 * และ unit test จะต้องยก database ขึ้นมาด้วยทั้งที่ควรทดสอบแค่การแปลงข้อมูล
 */
@Component
public class BookMapper {

    public BookResponse toResponse(Book book, long availableCopies, long totalCopies) {
        List<String> authorNames = book.getAuthors().stream()
                .map(Author::getFullName)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublishYear(),
                book.getPrice(),
                book.getCategory().getName(),
                book.getPublisher().getName(),
                authorNames,
                availableCopies,
                totalCopies);
    }

    public BookCopyResponse toCopyResponse(BookCopy copy) {
        return new BookCopyResponse(
                copy.getId(),
                copy.getBarcode(),
                copy.getBook().getTitle(),
                copy.getStatus().name(),
                copy.getShelfLocation(),
                copy.getAcquiredAt());
    }

    public List<BookCopyResponse> toCopyResponses(List<BookCopy> copies) {
        return copies.stream().map(this::toCopyResponse).toList();
    }
}
