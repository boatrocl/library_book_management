package com.libraflow.library.service;

import com.libraflow.library.dto.response.BookReferenceOptionsResponse;

public interface BookReferenceQueryService {
    BookReferenceOptionsResponse findAll();
}
