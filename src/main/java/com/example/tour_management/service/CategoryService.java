package com.example.tour_management.service;

import com.example.tour_management.dto.category.*;
import com.example.tour_management.entity.Category;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger log =
            LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAll() {
        log.info("Lấy danh sách category");

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(Integer id) {

        log.info("Lấy category id={}", id);

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy category id={}", id);
                    return new NotFoundException("Category not found");
                });

        return toResponse(c);
    }

    public CategoryResponse create(CategoryRequest req) {

        log.info("Tạo category: {}", req.getCategoryName());

        if (req.getCategoryName() == null || req.getCategoryName().isBlank()) {
            throw new BadRequestException("Tên category không được để trống");
        }

        if (categoryRepository.existsByCategoryName(req.getCategoryName())) {
            log.warn("Category đã tồn tại: {}", req.getCategoryName());
            throw new BadRequestException("Category đã tồn tại");
        }

        Category c = new Category();
        c.setCategoryName(req.getCategoryName());
        c.setDescription(req.getDescription());

        Category saved = categoryRepository.save(c);

        log.info("Tạo category thành công id={}", saved.getId());

        return toResponse(saved);
    }

    public CategoryResponse update(Integer id, CategoryRequest req) {

        log.info("Update category id={}", id);

        Category old = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy category id={}", id);
                    return new NotFoundException("Category not found");
                });

        if (req.getCategoryName() == null || req.getCategoryName().isBlank()) {
            throw new BadRequestException("Tên category không được để trống");
        }

        old.setCategoryName(req.getCategoryName());
        old.setDescription(req.getDescription());

        Category updated = categoryRepository.save(old);

        log.info("Update thành công id={}", id);

        return toResponse(updated);
    }

    public void delete(Integer id) {

        log.warn("Xóa category id={}", id);

        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found");
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category c) {

        CategoryResponse res = new CategoryResponse();
        res.setId(c.getId());
        res.setCategoryName(c.getCategoryName());
        res.setDescription(c.getDescription());

        return res;
    }
}