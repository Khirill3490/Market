package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCartIdOrderByCreatedAtAsc(Long cartId);

    Optional<CartItem> findByPublicIdAndCartId(String publicId, Long cartId);

    Optional<CartItem> findByCartIdAndProductPublicId(Long cartId, String productPublicId);
}