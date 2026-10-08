package com.libraflow.library.service.impl;

import com.libraflow.library.common.BarcodeGenerator;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.domain.entity.Publisher;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.BookMapper;
import com.libraflow.library.repository.AuthorRepository;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.CategoryRepository;
import com.libraflow.library.repository.PublisherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ทดสอบกฎทางธุรกิจของงานเขียนข้อมูลหนังสือ
 *
 * ที่ mock repository ได้ทั้งหมดเพราะ BookCommandServiceImpl ขึ้นกับ interface
 * และรับ dependency ผ่าน constructor (SOLID - D) ถ้าเคยเขียน new ...Repository()
 * ไว้ในคลาสนั้น จะ mock ไม่ได้เลยและต้องยกฐานข้อมูลจริงขึ้นมาทดสอบ
 */
@ExtendWith(MockitoExtension.class)
class BookCommandServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private BookMapper bookMapper;
    @Mock private BarcodeGenerator barcodeGenerator;

    @InjectMocks private BookCommandServiceImpl service;

    private CreateBookRequest sampleRequest() {
        return new CreateBookRequest("9780132350884", "Clean Code", 2008,
                new BigDecimal("1650.00"), 1L, 1L, List.of(1L));
    }

    @Test
    @DisplayName("สร้างหนังสือด้วย ISBN ที่มีอยู่แล้ว ต้องได้ ISBN_ALREADY_EXISTS และไม่บันทึกอะไรลงฐานข้อมูล")
    void shouldRejectDuplicateIsbn() {
        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ISBN_ALREADY_EXISTS));

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("สร้างหนังสือด้วยหมวดหมู่ที่ไม่มีอยู่ ต้องได้ 404 ไม่ใช่ 409")
    void shouldRejectMissingCategory() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-11 ลบหนังสือที่ยังมีตัวเล่มถูกยืมหรือถูกจอง ต้องได้ BOOK_IN_USE")
    void shouldRejectDeleteWhenCopiesAreInUse() {
        Book book = new Book("9780132350884", "Clean Code", 2008, new BigDecimal("1650.00"),
                new Category("Software Engineering", null),
                new Publisher("Prentice Hall", "United States"));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookCopyRepository.existsByBookIdAndStatusIn(
                1L, List.of(BookCopyStatus.ON_LOAN, BookCopyStatus.RESERVED))).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BOOK_IN_USE));

        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("ลบหนังสือที่ไม่มีตัวเล่มค้างอยู่ ต้องลบได้ตามปกติ")
    void shouldDeleteWhenNoCopyInUse() {
        Book book = new Book("9780132350884", "Clean Code", 2008, new BigDecimal("1650.00"),
                new Category("Software Engineering", null),
                new Publisher("Prentice Hall", "United States"));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookCopyRepository.existsByBookIdAndStatusIn(anyLong(), any())).thenReturn(false);

        service.delete(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("ลบหนังสือที่ไม่มีอยู่ ต้องได้ 404 และไม่ไปตรวจสถานะตัวเล่มต่อ")
    void shouldRejectDeleteWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookCopyRepository, never()).existsByBookIdAndStatusIn(anyLong(), any());
    }
}
