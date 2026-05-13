package ru.example.userservice.service;

import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.enums.OrderStatus;

public interface OrderStatusTransitionService {

    void recordInitialStatus(Order order);

    void changeStatus(
            Order order,
            OrderStatus newStatus,
            String reason
    );
}