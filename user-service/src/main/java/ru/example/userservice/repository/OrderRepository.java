package ru.example.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {
            "items",
            "deliveryAddress"
    })
    List<Order> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);

    @EntityGraph(attributePaths = {
            "items",
            "deliveryAddress"
    })
    Optional<Order> findByPublicIdAndAccountId(String publicId, Long accountId);

    @EntityGraph(attributePaths = {
            "items",
            "deliveryAddress",
            "account"
    })
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "items",
            "deliveryAddress",
            "account"
    })
    Optional<Order> findByPublicId(String publicId);
}