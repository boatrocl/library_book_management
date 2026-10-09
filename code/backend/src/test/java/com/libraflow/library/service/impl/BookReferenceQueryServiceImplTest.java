package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Author;
import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.domain.entity.Publisher;
import com.libraflow.library.dto.response.BookReferenceOptionsResponse;
import com.libraflow.library.repository.AuthorRepository;
import com.libraflow.library.repository.CategoryRepository;
import com.libraflow.library.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookReferenceQueryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private AuthorRepository authorRepository;
    @InjectMocks
    private BookReferenceQueryServiceImpl service;

    @Test
    void findAll_returnsCurrentDatabaseIdsAndNames() {
        Category category = org.mockito.Mockito.mock(Category.class);
        when(category.getId()).thenReturn(7L);
        when(category.getName()).thenReturn("Software Engineering");
        Publisher publisher = org.mockito.Mockito.mock(Publisher.class);
        when(publisher.getId()).thenReturn(8L);
        when(publisher.getName()).thenReturn("Example Press");
        Author author = org.mockito.Mockito.mock(Author.class);
        when(author.getId()).thenReturn(9L);
        when(author.getFullName()).thenReturn("A. Writer");
        when(categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(category));
        when(publisherRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(publisher));
        when(authorRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"))).thenReturn(List.of(author));

        BookReferenceOptionsResponse response = service.findAll();

        assertThat(response.categories()).containsExactly(new com.libraflow.library.dto.response.BookReferenceOptionResponse(7L, "Software Engineering"));
        assertThat(response.publishers()).containsExactly(new com.libraflow.library.dto.response.BookReferenceOptionResponse(8L, "Example Press"));
        assertThat(response.authors()).containsExactly(new com.libraflow.library.dto.response.BookReferenceOptionResponse(9L, "A. Writer"));
        verify(categoryRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
        verify(publisherRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
        verify(authorRepository).findAll(Sort.by(Sort.Direction.ASC, "fullName"));
    }
}
