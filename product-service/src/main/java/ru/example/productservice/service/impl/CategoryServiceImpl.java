package ru.example.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.productservice.entity.Category;
import ru.example.productservice.exception.CategoryNotFoundException;
import ru.example.productservice.mapper.CategoryMapper;
import ru.example.productservice.repository.CategoryRepository;
import ru.example.productservice.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> findAllAndGetProductsCount() {
        return categoryRepository
                .findAllWithProductCount()
                .stream()
                .map(categoryMapper::fromDtoToCategory)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> findCategoryForMain() {
        return categoryRepository.findRandom6();
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Категория с названием '" + name + "' не найдена"
                ));
    }

    @Override
    public Category findByIdAndGetProductsCount(Long id) {
        var categoryWithCount = categoryRepository.findByIdWithProductCount(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        return categoryMapper.fromDtoToCategory(categoryWithCount);
    }

    @Override
    public Category findByNameAndGetProductsCount(String name) {
        throw new UnsupportedOperationException("Поиск категории с количеством товаров по name пока не реализован");
    }

    @Override
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }
}