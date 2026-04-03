package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.productservice.entity.Brand;


import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByNameEqualsIgnoreCase(String brand);
}
