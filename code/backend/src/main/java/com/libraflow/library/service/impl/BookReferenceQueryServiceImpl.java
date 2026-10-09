package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Author;
import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.domain.entity.Publisher;
import com.libraflow.library.dto.response.BookReferenceOptionResponse;
import com.libraflow.library.dto.response.BookReferenceOptionsResponse;
import com.libraflow.library.repository.AuthorRepository;
import com.libraflow.library.repository.CategoryRepository;
import com.libraflow.library.repository.PublisherRepository;
import com.libraflow.library.service.BookReferenceQueryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reads the current reference data so the book editor never depends on hardcoded IDs. */
@Service
@Transactional(readOnly = true)
public class BookReferenceQueryServiceImpl implements BookReferenceQueryService {

    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    public BookReferenceQueryServiceImpl(CategoryRepository categoryRepository,
                                         PublisherRepository publisherRepository,
                                         AuthorRepository authorRepository) {
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
    }

    @Override
    public BookReferenceOptionsResponse findAll() {
        List<BookReferenceOptionResponse> categories = categoryRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(category -> option(category.getId(), category.getName()))
                .toList();
        List<BookReferenceOptionResponse> publishers = publisherRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(publisher -> option(publisher.getId(), publisher.getName()))
                .toList();
        List<BookReferenceOptionResponse> authors = authorRepository
                .findAll(Sort.by(Sort.Direction.ASC, "fullName")).stream()
                .map(author -> option(author.getId(), author.getFullName()))
                .toList();

        return new BookReferenceOptionsResponse(categories, publishers, authors);
    }

    private static BookReferenceOptionResponse option(Long id, String name) {
        return new BookReferenceOptionResponse(id, name);
    }
}
