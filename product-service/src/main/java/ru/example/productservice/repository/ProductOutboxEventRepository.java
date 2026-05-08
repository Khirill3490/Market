package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.productservice.entity.ProductOutboxEvent;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, Long> {
}