package com.libraflow.library.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.enums.BookAvailabilityFilter;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.BookMapper;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.projection.BookCopyCount;

/**
 * ทดสอบงานอ่านข้อมูลหนังสือ (BookQueryServiceImpl)
 *
 * mock ได้ทั้งหมดเพราะ service ขึ้นกับ interface และรับ dependency ผ่าน constructor
 * (SOLID - D) เหมือนที่อธิบายไว้ใน BookCommandServiceImplTest
 */
@ExtendWith(MockitoExtension.class)
class BookQueryServiceImplTest {

  @Mock private BookRepository bookRepository;
  @Mock private BookCopyRepository bookCopyRepository;
  @Mock private BookMapper bookMapper;

  @InjectMocks private BookQueryServiceImpl service;

  /**
   * Book ไม่มี setId (JPA เป็นคนใส่ id) และ constructor ว่างเป็น protected
   * จึงใช้ mock แล้วสั่งให้ getId() ตอบ เพราะ search อ่านจาก Book แค่ id เพื่อจับคู่ผลนับตัวเล่ม
   */
  private Book bookWithId(Long id) {
    Book book = mock(Book.class);
    when(book.getId()).thenReturn(id);
    return book;
  }

  private BookResponse responseOf(Long id) {
    return new BookResponse(id, "9780132350884", "Clean Code", null, null, null, null, null, 0L, 0L);
  }

  // ---------------------------------------------------------------- findById / findCopies

  @Test
  @DisplayName("เมื่อค้นหาหนังสือด้วย id ที่ไม่มีในระบบ จะโยน ResourceNotFoundException")
  void findByIdShouldThrowWhenNotFound() {

    when(bookRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("ไม่พบ หนังสือ ที่มี id = 1");

    verify(bookCopyRepository, never()).countByBookId(anyLong());
  }

  @Test
  @DisplayName("findCopies ด้วย id ที่ไม่มีในระบบ จะโยน ResourceNotFoundException")
  void findCopiesByBookIdShouldThrowWhenNotFound() {

    when(bookRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> service.findCopies(1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("ไม่พบ หนังสือ ที่มี id = 1");

    verify(bookCopyRepository, never()).findByBookIdOrderByBarcodeAsc(anyLong());
  }

  @Test
  @DisplayName("findCopies เจอหนังสือ คืนรายการตัวเล่มที่ mapper แปลงแล้ว")
  void shouldFindCopiesWhenBookExists() {

    List<BookCopyResponse> expected = List.of(new BookCopyResponse(null, "BC-001", null, null, null, null));
    List<BookCopy> mockBookcopies = List.of(new BookCopy("BC-001", null, null, null));
    when(bookRepository.existsById(1L)).thenReturn(true);
    when(bookCopyRepository.findByBookIdOrderByBarcodeAsc(1L)).thenReturn(mockBookcopies);
    when(bookMapper.toCopyResponses(mockBookcopies)).thenReturn(expected);

    List<BookCopyResponse> actual = service.findCopies(1L);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  @DisplayName("findById เมื่อพบหนังสือ ต้องส่งจำนวนตัวเล่มทั้งหมดและที่ว่างให้ mapper ถูกช่อง")
  void shouldFindByIdWhenBookExists() {

    Book book = new Book("9780132350884", "Clean Code", null, null, null, null);
    BookResponse expected = new BookResponse(null, "9780132350884", "Clean Code", null, null, null, null, null, 5L, 10L);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(bookCopyRepository.countByBookId(1L)).thenReturn(10L);
    when(bookCopyRepository.countByBookIdAndStatus(1L, BookCopyStatus.AVAILABLE)).thenReturn(5L);
    when(bookMapper.toResponse(book, 5L, 10L)).thenReturn(expected);

    BookResponse actual = service.findById(1L);

    assertThat(actual).isSameAs(expected);

    // ลำดับพารามิเตอร์ของ toResponse คือ (book, availableCopies, totalCopies)
    verify(bookMapper).toResponse(book, 5L, 10L);
  }

  // ---------------------------------------------------------------- search

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("search ด้วย keyword ที่เป็น null, ว่าง หรือเป็นช่องว่างล้วน ต้องส่ง \"\" ให้ repository")
  void searchShouldPassEmptyKeywordWhenKeywordIsBlank(String keyword) {

    Pageable pageable = PageRequest.of(0, 10);
    when(bookRepository.search("", null, "ALL", pageable)).thenReturn(Page.empty(pageable));

    service.search(keyword, null, null, pageable);

    // ถ้า service ส่ง null ไปจริง จะเกิด error lower(bytea) บน PostgreSQL
    verify(bookRepository).search("", null, "ALL", pageable);
  }

  @Test
  @DisplayName("search ต้องตัดช่องว่างหน้าหลัง keyword ก่อนส่งให้ repository")
  void searchShouldTrimKeywordBeforeQuerying() {

    Pageable pageable = PageRequest.of(0, 10);
    when(bookRepository.search("clean", null, "ALL", pageable)).thenReturn(Page.empty(pageable));

    service.search("  clean  ", null, null, pageable);

    verify(bookRepository).search("clean", null, "ALL", pageable);
  }

  @Test
  @DisplayName("search เมื่อไม่ได้ส่ง availability ต้องใช้ ALL (ไม่กรองตามความพร้อมให้ยืม)")
  void searchShouldDefaultAvailabilityToAllWhenNull() {

    Pageable pageable = PageRequest.of(0, 10);
    when(bookRepository.search("clean", null, "ALL", pageable)).thenReturn(Page.empty(pageable));

    service.search("clean", null, null, pageable);

    verify(bookRepository).search("clean", null, "ALL", pageable);
  }

  @Test
  @DisplayName("search ต้องส่งชื่อ enum ของ availability ที่เลือกให้ repository เป็น String")
  void searchShouldPassAvailabilityNameToRepository() {

    Pageable pageable = PageRequest.of(0, 10);
    when(bookRepository.search("clean", 7L, "AVAILABLE", pageable)).thenReturn(Page.empty(pageable));

    service.search("clean", 7L, BookAvailabilityFilter.AVAILABLE, pageable);

    verify(bookRepository).search("clean", 7L, "AVAILABLE", pageable);
  }

  @Test
  @DisplayName("search เมื่อไม่พบหนังสือเลย ต้องไม่ยิง query นับตัวเล่ม (กัน IN () ว่าง)")
  void searchShouldNotCountCopiesWhenPageIsEmpty() {

    Pageable pageable = PageRequest.of(0, 10);
    when(bookRepository.search("clean", null, "ALL", pageable)).thenReturn(Page.empty(pageable));

    PageResponse<BookResponse> result = service.search("clean", null, null, pageable);

    assertThat(result.content()).isEmpty();
    verify(bookCopyRepository, never()).countCopiesByBookIds(org.mockito.ArgumentMatchers.anyCollection());
  }

  @Test
  @DisplayName("search เมื่อหนังสือไม่มีแถวผลนับตัวเล่ม ต้องส่ง 0 และ 0 ให้ mapper")
  void searchShouldUseZeroCountsWhenBookHasNoCountRow() {

    Pageable pageable = PageRequest.of(0, 10);
    Book book = bookWithId(1L);
    BookResponse expected = responseOf(1L);

    when(bookRepository.search("clean", null, "ALL", pageable))
        .thenReturn(new PageImpl<>(List.of(book), pageable, 1));
    // หนังสือเล่มนี้ยังไม่มีตัวเล่มเลย จึงไม่มีแถวกลับมา
    when(bookCopyRepository.countCopiesByBookIds(List.of(1L))).thenReturn(List.of());
    when(bookMapper.toResponse(book, 0L, 0L)).thenReturn(expected);

    PageResponse<BookResponse> result = service.search("clean", null, null, pageable);

    assertThat(result.content()).containsExactly(expected);
  }

  @Test
  @DisplayName("search เมื่อพบหนังสือหลายเล่มในหน้าเดียว ต้องนับตัวเล่มด้วย query เดียว ไม่นับทีละเล่ม (ป้องกัน N+1)")
  void searchShouldCountCopiesOncePerPageWhenMultipleBooksFound() {

    Pageable pageable = PageRequest.of(0, 10);
    Book book1 = bookWithId(1L);
    Book book2 = bookWithId(2L);
    Book book3 = bookWithId(3L);

    when(bookRepository.search("clean", null, "ALL", pageable))
        .thenReturn(new PageImpl<>(List.of(book1, book2, book3), pageable, 3));
    // BookCopyCount เรียงเป็น (bookId, totalCopies, availableCopies)
    when(bookCopyRepository.countCopiesByBookIds(List.of(1L, 2L, 3L))).thenReturn(List.of(
        new BookCopyCount(1L, 10L, 5L),
        new BookCopyCount(2L, 4L, 4L),
        new BookCopyCount(3L, 2L, 0L)));

    service.search("clean", null, null, pageable);

    // หัวใจของเทส: นับครั้งเดียวทั้งหน้า และไม่มีการนับทีละเล่ม
    verify(bookCopyRepository, times(1)).countCopiesByBookIds(List.of(1L, 2L, 3L));
    verify(bookCopyRepository, never()).countByBookId(anyLong());
    verify(bookCopyRepository, never()).countByBookIdAndStatus(anyLong(), org.mockito.ArgumentMatchers.any());

    // และผลนับถูกจับคู่กับหนังสือถูกเล่ม โดย toResponse รับ (book, available, total)
    verify(bookMapper).toResponse(book1, 5L, 10L);
    verify(bookMapper).toResponse(book2, 4L, 4L);
    verify(bookMapper).toResponse(book3, 0L, 2L);
  }
}
