package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByAccountId(Long accountId);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByPublicIdAndAccountId(String publicId, Long accountId);
}