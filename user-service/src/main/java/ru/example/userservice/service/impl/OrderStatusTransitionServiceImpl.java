package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.OrderStatusHistory;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.repository.OrderStatusHistoryRepository;
import ru.example.userservice.service.OrderStatusTransitionService;

@Service
@RequiredArgsConstructor
public class OrderStatusTransitionServiceImpl implements OrderStatusTransitionService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public void recordInitialStatus(Order order) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(null)
                .toStatus(order.getStatus())
                .reason(order.getStatusReason())
                .build();

        orderStatusHistoryRepository.save(history);
    }

    @Override
    public void changeStatus(
            Order order,
            OrderStatus newStatus,
            String reason
    ) {
        OrderStatus previousStatus = order.getStatus();

        order.setStatus(newStatus);
        order.setStatusReason(reason);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(previousStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        orderStatusHistoryRepository.save(history);
    }
}