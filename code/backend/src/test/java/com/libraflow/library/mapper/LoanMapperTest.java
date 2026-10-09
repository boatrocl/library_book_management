package com.libraflow.library.mapper;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.dto.response.LoanItemResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoanMapperTest {

    @Test
    void toItemResponse_includesBookIdForOpeningBookDetails() {
        LoanItem item = mock(LoanItem.class);
        BookCopy copy = mock(BookCopy.class);
        Book book = mock(Book.class);
        when(item.getId()).thenReturn(31L);
        when(item.getBookCopy()).thenReturn(copy);
        when(item.getDueDate()).thenReturn(LocalDate.of(2026, 10, 17));
        when(item.getReturnedAt()).thenReturn(null);
        when(copy.getBook()).thenReturn(book);
        when(copy.getBarcode()).thenReturn("LIB-00031");
        when(book.getId()).thenReturn(42L);
        when(book.getTitle()).thenReturn("Clean Code");

        LoanItemResponse response = new LoanMapper().toItemResponse(item);

        assertThat(response.bookId()).isEqualTo(42L);
        assertThat(response.bookTitle()).isEqualTo("Clean Code");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 10, 17));
    }
}
