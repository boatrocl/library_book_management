package com.libraflow.library.service;

import com.libraflow.library.dto.response.CategoryResponse;

import java.util.List;

/** Read-only category operations for public catalogue clients. */
public interface CategoryQueryService {

    List<CategoryResponse> findAll();
}
