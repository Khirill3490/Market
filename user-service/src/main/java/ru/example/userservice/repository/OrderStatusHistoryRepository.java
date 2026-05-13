package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}