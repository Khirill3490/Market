package ru.example.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.example.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findByArtIgnoreCase(String art);

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findByPublicId(String publicId);

    boolean existsByArtIgnoreCase(String art);

    @Query("""
            select p
            from Product p
            join fetch p.brand
            join fetch p.category
            """)
    List<Product> findAllWithBrandAndCategory();
}