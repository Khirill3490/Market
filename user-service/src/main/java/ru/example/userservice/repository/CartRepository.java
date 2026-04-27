package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByAccountId(Long accountId);

    Optional<Cart> findByPublicIdAndAccountId(String publicId, Long accountId);
}