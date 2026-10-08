package com.libraflow.library.service.impl;

import com.libraflow.library.common.BarcodeGenerator;
import com.libraflow.library.domain.entity.Author;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.domain.entity.Publisher;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.dto.request.CreateBookCopyRequest;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.request.UpdateBookRequest;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.BookMapper;
import com.libraflow.library.repository.AuthorRepository;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.CategoryRepository;
import com.libraflow.library.repository.PublisherRepository;
import com.libraflow.library.service.BookCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * งานเขียนข้อมูลหนังสือทั้งหมด
 *
 * Transactional ครอบทุกเมธอด (ไม่มี readOnly) ถ้าขั้นตอนใดล้มเหลวกลางทาง
 * ข้อมูลที่เขียนไปแล้วจะถูก rollback ทั้งชุด เพราะ BusinessException สืบทอด
 * RuntimeException ซึ่ง Spring rollback ให้อัตโนมัติ
 *
 * SOLID - D: dependency ทุกตัวเป็น interface รับผ่าน constructor เดียว
 */
@Service
@Transactional
public class BookCommandServiceImpl implements BookCommandService {

    private static final String BARCODE_PREFIX = "LIB-";

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;
    private final BarcodeGenerator barcodeGenerator;

    public BookCommandServiceImpl(BookRepository bookRepository,
                                  BookCopyRepository bookCopyRepository,
                                  CategoryRepository categoryRepository,
                                  PublisherRepository publisherRepository,
                                  AuthorRepository authorRepository,
                                  BookMapper bookMapper,
                                  BarcodeGenerator barcodeGenerator) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
        this.barcodeGenerator = barcodeGenerator;
    }

    @Override
    public BookResponse create(CreateBookRequest request) {
        // ตรวจ ISBN ซ้ำที่ระดับโค้ดเพื่อตอบ 409 พร้อมข้อความที่อ่านรู้เรื่อง
        // ส่วน UNIQUE constraint ในฐานข้อมูลเป็นด่านสุดท้ายกันกรณีสองคนกดพร้อมกัน
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new BusinessException(ErrorCode.ISBN_ALREADY_EXISTS,
                    "ISBN " + request.isbn() + " มีอยู่ในระบบแล้ว");
        }

        Category category = loadCategory(request.categoryId());
        Publisher publisher = loadPublisher(request.publisherId());
        List<Author> authors = loadAuthors(request.authorIds());

        Book book = new Book(request.isbn(), request.title(), request.publishYear(),
                request.price(), category, publisher);
        authors.forEach(book::addAuthor);

        Book saved = bookRepository.save(book);

        // หนังสือที่เพิ่งสร้างยังไม่มีตัวเล่ม จำนวนจึงเป็น 0 ทั้งคู่
        return bookMapper.toResponse(saved, 0L, 0L);
    }

    @Override
    public BookResponse update(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("หนังสือ", id));

        // ยอมให้ ISBN เดิมของตัวเองผ่าน แต่ห้ามไปชนของเล่มอื่น
        bookRepository.findByIsbn(request.isbn())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.ISBN_ALREADY_EXISTS,
                            "ISBN " + request.isbn() + " ถูกใช้โดยหนังสือเล่มอื่นแล้ว");
                });

        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setPublishYear(request.publishYear());
        book.setPrice(request.price());
        book.setCategory(loadCategory(request.categoryId()));
        book.setPublisher(loadPublisher(request.publisherId()));
        book.replaceAuthors(new LinkedHashSet<>(loadAuthors(request.authorIds())));

        // ไม่ต้องเรียก save() เพราะ entity อยู่ในสถานะ managed ภายใน transaction
        // Hibernate ตรวจความเปลี่ยนแปลงเอง (dirty checking) แล้ว UPDATE ให้ตอน commit

        long total = bookCopyRepository.countByBookId(id);
        long available = bookCopyRepository.countByBookIdAndStatus(id, BookCopyStatus.AVAILABLE);
        return bookMapper.toResponse(book, available, total);
    }

    @Override
    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("หนังสือ", id));

        // BR-11 ลบหนังสือไม่ได้ถ้ายังมีตัวเล่มที่ถูกยืมหรือถูกกันไว้ให้คิวจอง
        boolean inUse = bookCopyRepository.existsByBookIdAndStatusIn(
                id, List.of(BookCopyStatus.ON_LOAN, BookCopyStatus.RESERVED));

        if (inUse) {
            throw new BusinessException(ErrorCode.BOOK_IN_USE,
                    "ลบหนังสือ " + book.getTitle() + " ไม่ได้ เพราะยังมีตัวเล่มที่ถูกยืมหรือถูกจองอยู่");
        }

        bookRepository.delete(book);
    }

    @Override
    public BookCopyResponse addCopy(Long bookId, CreateBookCopyRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("หนังสือ", bookId));

        String barcode = resolveBarcode(request.barcode());
        LocalDate acquiredAt = request.acquiredAt() != null ? request.acquiredAt() : LocalDate.now();

        BookCopy copy = new BookCopy(barcode, book, request.shelfLocation(), acquiredAt);
        BookCopy saved = bookCopyRepository.save(copy);

        return bookMapper.toCopyResponse(saved);
    }

    /** ถ้าผู้ใช้ไม่ได้ระบุบาร์โค้ดมา ให้ระบบสร้างต่อจากเลขล่าสุด */
    private String resolveBarcode(String requestedBarcode) {
        if (requestedBarcode != null && !requestedBarcode.isBlank()) {
            String barcode = requestedBarcode.trim();
            if (bookCopyRepository.existsByBarcode(barcode)) {
                throw new BusinessException(ErrorCode.BARCODE_ALREADY_EXISTS,
                        "บาร์โค้ด " + barcode + " มีอยู่ในระบบแล้ว");
            }
            return barcode;
        }
        String currentMax = bookCopyRepository.findMaxBarcode(BARCODE_PREFIX).orElse(null);
        return barcodeGenerator.next(currentMax);
    }

    private Category loadCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("หมวดหมู่", categoryId));
    }

    private Publisher loadPublisher(Long publisherId) {
        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("สำนักพิมพ์", publisherId));
    }

    /** ดึงผู้แต่งทุกคนด้วย query เดียว และตรวจว่าไม่มี id ไหนหาไม่เจอ */
    private List<Author> loadAuthors(List<Long> authorIds) {
        List<Author> authors = authorRepository.findByIdIn(authorIds);
        if (authors.size() != authorIds.size()) {
            List<Long> foundIds = authors.stream().map(Author::getId).toList();
            List<Long> missing = authorIds.stream().filter(aid -> !foundIds.contains(aid)).toList();
            throw new ResourceNotFoundException("ไม่พบผู้แต่งที่มี id = " + missing);
        }
        return authors;
    }
}
