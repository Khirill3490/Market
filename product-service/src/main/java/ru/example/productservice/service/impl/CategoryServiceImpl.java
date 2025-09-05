package ru.example.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.example.common.entity.Category;
import ru.example.common.exception.EntityNotFoundException;
import ru.example.productservice.mapper.CategoryMapper;
import ru.example.productservice.repository.CategoryRepository;
import ru.example.productservice.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
                .orElseThrow(() -> new EntityNotFoundException("Ошибка. Категория с id " + id + " не найдена"));

    }

    @Override
    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Ошибка. Категория по названию " + name
                        + " не найдена"));

    }

    @Override
    public Category findByIdAndGetProductsCount(Long id) {
        var optCategory = categoryRepository.findByIdWithProductCount(id)
                .orElseThrow(() -> new EntityNotFoundException("Ошибка. Категория с id " + id + " не найдена"));
        return categoryMapper.fromDtoToCategory(optCategory);
    }

    @Override
    public Category findByNameAndGetProductsCount(String name) {
        return null;
    }

    @Override
    public Category save(Category category) {
        return null;
    }

    @Override
    public Category update(Category category) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
