package ru.example.userservice.mapper;

import org.springframework.stereotype.Component;
import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.OrderItem;
import ru.example.userservice.model.response.OrderItemResponse;
import ru.example.userservice.model.response.OrderResponse;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        int totalItems = items.stream()
                .mapToInt(OrderItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(OrderItemResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getPublicId(),
                order.getStatus(),
                order.getStatusReason(),
                order.getDeliveryAddress().getPublicId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items,
                totalItems,
                totalAmount
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductPublicId(),
                item.getProductName(),
                item.getProductImage(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}