package com.libraflow.library.mapper;

import com.libraflow.library.domain.entity.Author;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.domain.entity.Publisher;
import com.libraflow.library.dto.response.BookResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BookMapper ไม่แตะ repository เลย จึงทดสอบด้วย object ธรรมดาได้
 * ไม่ต้องยก Spring Context หรือฐานข้อมูล — เทสต์จบในหลักมิลลิวินาที
 */
class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    @DisplayName("แปลง Book เป็น BookResponse ครบทุก field และแปลงผู้แต่งเป็นชื่อ")
    void shouldMapBookToResponse() {
        Category category = new Category("Software Engineering", "วิศวกรรมซอฟต์แวร์");
        Publisher publisher = new Publisher("Prentice Hall", "United States");
        Book book = new Book("9780132350884", "Clean Code", 2008,
                new BigDecimal("1650.00"), category, publisher);
        book.addAuthor(new Author("Robert C. Martin", "American", null));

        BookResponse response = mapper.toResponse(book, 3L, 5L);

        assertThat(response.isbn()).isEqualTo("9780132350884");
        assertThat(response.title()).isEqualTo("Clean Code");
        assertThat(response.publishYear()).isEqualTo(2008);
        assertThat(response.price()).isEqualByComparingTo("1650.00");
        assertThat(response.categoryName()).isEqualTo("Software Engineering");
        assertThat(response.publisherName()).isEqualTo("Prentice Hall");
        assertThat(response.authors()).containsExactly("Robert C. Martin");
        assertThat(response.availableCopies()).isEqualTo(3L);
        assertThat(response.totalCopies()).isEqualTo(5L);
    }

    @Test
    @DisplayName("หนังสือที่มีผู้แต่งหลายคนต้องเรียงชื่อให้คงที่ ไม่สลับไปมาในแต่ละครั้ง")
    void shouldSortAuthorNames() {
        Book book = new Book("9780201633610", "Design Patterns", 1994,
                new BigDecimal("2100.00"),
                new Category("Software Engineering", null),
                new Publisher("Addison-Wesley", "United States"));
        book.addAuthor(new Author("Richard Helm", "Australian", null));
        book.addAuthor(new Author("Erich Gamma", "Swiss", null));
        book.addAuthor(new Author("John Vlissides", "American", null));

        BookResponse response = mapper.toResponse(book, 0L, 2L);

        assertThat(response.authors())
                .containsExactly("Erich Gamma", "John Vlissides", "Richard Helm");
    }

    @Test
    @DisplayName("หนังสือที่ยังไม่มีตัวเล่มต้องได้จำนวนเป็นศูนย์ ไม่ใช่ null")
    void shouldHandleBookWithoutCopies() {
        Book book = new Book("9781617297571", "Spring in Action", 2022,
                new BigDecimal("2250.00"),
                new Category("Programming Languages", null),
                new Publisher("Manning Publications", "United States"));
        book.addAuthor(new Author("Craig Walls", "American", null));

        BookResponse response = mapper.toResponse(book, 0L, 0L);

        assertThat(response.availableCopies()).isZero();
        assertThat(response.totalCopies()).isZero();
    }
}
