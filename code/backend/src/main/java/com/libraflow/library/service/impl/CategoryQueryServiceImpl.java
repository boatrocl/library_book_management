package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Category;
import com.libraflow.library.dto.response.CategoryResponse;
import com.libraflow.library.repository.CategoryRepository;
import com.libraflow.library.service.CategoryQueryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryQueryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(CategoryQueryServiceImpl::toResponse)
                .toList();
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
