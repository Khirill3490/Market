package ru.example.productservice.service;


import ru.example.common.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAllAndGetProductsCount();
    List<Category> findAll();

    List<Category> findCategoryForMain();


    Category findById(Long id);
    Category findByName(String name);

    Category findByIdAndGetProductsCount(Long id);
    Category findByNameAndGetProductsCount(String name);

    Category save(Category category);
    Category update(Category category);
    void deleteById(Long id);
}
