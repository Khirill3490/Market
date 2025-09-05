package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.common.entity.Category;
import ru.example.productservice.dto.CategoryWithCountDto;

import ru.example.productservice.model.response.CategoryResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    @Query("""
        SELECT new ru.example.productservice.dto.CategoryWithCountDto(c.id, c.name, COUNT(p.id))
        FROM Category c
        LEFT JOIN Product p ON p.category = c
        GROUP BY c.id, c.name
        """)
    List<CategoryWithCountDto> findAllWithProductCount();

    @Query("""
    SELECT new ru.example.productservice.dto.CategoryWithCountDto(c.id, c.name, COUNT(p.id))
    FROM Category c
    LEFT JOIN Product p ON p.category = c
    WHERE c.id = :categoryId
    GROUP BY c.id, c.name
    """)
    Optional<CategoryWithCountDto> findByIdWithProductCount(@Param("categoryId") Long id);

    @Query(value = "SELECT * FROM category ORDER BY RANDOM() LIMIT 6", nativeQuery = true)
    List<Category> findRandom6();

    Optional<Category> findByName(String name);
}
