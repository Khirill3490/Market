package ru.example.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.example.productservice.entity.Product;


import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Product> findByArtContainingIgnoreCase(String art);

    boolean existsByArtContainingIgnoreCase(String art);

    @Query(value = "SELECT * FROM products ORDER BY RANDOM() LIMIT 8", nativeQuery = true)
    List<Product> findRandom8();

//    List<Product> findAllBy
}
