package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.exception.InvalidOrderStatusTransitionException;
import ru.example.userservice.exception.OrderNotFoundException;
import ru.example.userservice.mapper.OrderMapper;
import ru.example.userservice.model.request.UpdateOrderStatusRequest;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.repository.OrderRepository;
import ru.example.userservice.service.AdminOrderService;
import ru.example.userservice.service.OrderStatusTransitionService;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusTransitionService orderStatusTransitionService;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of(),
            OrderStatus.STOCK_RESERVATION_FAILED, Set.of(),
            OrderStatus.CANCELLATION_REQUESTED, Set.of(),
            OrderStatus.CANCELLATION_FAILED, Set.of(),
            OrderStatus.PENDING_STOCK_RESERVATION, Set.of()
    );

    @Override
    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return orderRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse getOrderByPublicId(String orderPublicId) {
        Order order = findOrderByPublicId(orderPublicId);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderPublicId, UpdateOrderStatusRequest request) {
        Order order = findOrderByPublicId(orderPublicId);

        OrderStatus currentStatus = order.getStatus();
        OrderStatus targetStatus = request.status();

        if (!canMoveTo(currentStatus, targetStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, targetStatus);
        }

        orderStatusTransitionService.changeStatus(
                order,
                targetStatus,
                "Статус заказа изменён администратором"
        );

        return orderMapper.toResponse(order);
    }

    private Order findOrderByPublicId(String orderPublicId) {
        return orderRepository
                .findByPublicId(orderPublicId)
                .orElseThrow(() -> new OrderNotFoundException(orderPublicId));
    }

    private boolean canMoveTo(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return true;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(currentStatus, Set.of())
                .contains(targetStatus);
    }

}