package ru.example.productservice.mapper;

import org.springframework.stereotype.Component;
import ru.example.productservice.entity.Category;
import ru.example.productservice.dto.CategoryWithCountDto;

import ru.example.productservice.model.response.CategoryResponse;
import ru.example.productservice.model.response.CategorySimpleResponse;

@Component
public class CategoryMapper {

    public Category fromDtoToCategory(CategoryWithCountDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .productCount(dto.getProductCount())
                .build();
    }

    public CategoryResponse fromCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .productCount(category.getProductCount())
                .build();
    }

    public CategorySimpleResponse toSimpleResponse(Category category) {
        return CategorySimpleResponse.builder()
                .name(category.getName())
                .build();
    }
}
