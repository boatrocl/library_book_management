package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.BookMapper;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.projection.BookCopyCount;
import com.libraflow.library.service.BookQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service Layer Pattern — รวม business logic และขอบเขต transaction ไว้ที่เดียว
 *
 * readOnly = true บอก Hibernate ว่า transaction นี้ไม่มีการแก้ข้อมูล จึงข้ามการทำ
 * dirty checking (การเทียบ snapshot ของ entity ตอน flush) ทำให้อ่านเร็วขึ้นและกัน
 * การเผลอเขียนข้อมูลในเมธอดที่ควรอ่านอย่างเดียว
 *
 * SOLID - D: field ทุกตัวเป็น private final ชนิด interface และรับผ่าน constructor
 * ตัวเดียว ไม่มี @Autowired บน field ที่ไหนในโปรเจคนี้ ผลคือใน unit test
 * สามารถ @Mock repository แล้วส่งเข้า constructor ได้เลยโดยไม่ต้องยก Spring Context
 */
@Service
@Transactional(readOnly = true)
public class BookQueryServiceImpl implements BookQueryService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookMapper bookMapper;

    public BookQueryServiceImpl(BookRepository bookRepository,
                                BookCopyRepository bookCopyRepository,
                                BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public PageResponse<BookResponse> search(String keyword, Long categoryId, Pageable pageable) {
        // ทำให้คำค้นที่เป็นช่องว่างล้วนมีความหมายเท่ากับไม่ได้ส่งมา
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Book> page = bookRepository.search(normalizedKeyword, categoryId, pageable);

        // นับตัวเล่มของหนังสือทุกเล่มในหน้านี้ด้วย query เดียว แทนการนับทีละเล่ม
        Map<Long, BookCopyCount> countsByBookId = loadCopyCounts(page.getContent());

        return PageResponse.from(page, book -> toResponseWithCounts(book, countsByBookId));
    }

    @Override
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("หนังสือ", id));

        long total = bookCopyRepository.countByBookId(id);
        long available = bookCopyRepository.countByBookIdAndStatus(
                id, com.libraflow.library.domain.enums.BookCopyStatus.AVAILABLE);

        return bookMapper.toResponse(book, available, total);
    }

    @Override
    public List<BookCopyResponse> findCopies(Long bookId) {
        // ตรวจก่อนว่าหนังสือมีจริง เพื่อแยก 404 (ไม่มีหนังสือ) ออกจาก 200 + list ว่าง (มีหนังสือแต่ยังไม่มีตัวเล่ม)
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("หนังสือ", bookId);
        }

        List<BookCopy> copies = bookCopyRepository.findByBookIdOrderByBarcodeAsc(bookId);
        return bookMapper.toCopyResponses(copies);
    }

    private Map<Long, BookCopyCount> loadCopyCounts(List<Book> books) {
        List<Long> bookIds = books.stream().map(Book::getId).toList();
        if (bookIds.isEmpty()) {
            // เลี่ยงการยิง query ที่มี IN () ว่าง ซึ่งเป็น SQL ที่ไม่ถูกต้อง
            return Map.of();
        }
        return bookCopyRepository.countCopiesByBookIds(bookIds).stream()
                .collect(java.util.stream.Collectors.toMap(BookCopyCount::bookId, Function.identity()));
    }

    private BookResponse toResponseWithCounts(Book book, Map<Long, BookCopyCount> counts) {
        BookCopyCount count = counts.get(book.getId());
        long total = count == null ? 0L : count.totalCopies();
        long available = count == null ? 0L : count.availableCopies();
        return bookMapper.toResponse(book, available, total);
    }
}
