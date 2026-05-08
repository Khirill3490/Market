package ru.example.productservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.productservice.entity.Product;

import java.util.Collection;
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


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Product p
        set p.stockQuantity = p.stockQuantity - :quantity
        where p.publicId = :publicId
          and p.stockQuantity >= :quantity
        """)
    int decreaseStockIfEnough(
            @Param("publicId") String publicId,
            @Param("quantity") int quantity
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from Product p
        where p.publicId in :publicIds
        """)
    List<Product> findAllByPublicIdInForUpdate(
            @Param("publicIds") Collection<String> publicIds
    );
}