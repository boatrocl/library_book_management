package com.libraflow.library.dto.response;

import java.util.List;

/** Current database values used to resolve book category, publisher, and author IDs. */
public record BookReferenceOptionsResponse(
        List<BookReferenceOptionResponse> categories,
        List<BookReferenceOptionResponse> publishers,
        List<BookReferenceOptionResponse> authors
) {
}
