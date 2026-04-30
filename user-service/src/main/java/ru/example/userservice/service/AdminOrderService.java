package ru.example.userservice.service;

import org.springframework.data.domain.Page;
import ru.example.userservice.model.request.UpdateOrderStatusRequest;
import ru.example.userservice.model.response.OrderResponse;

public interface AdminOrderService {

    Page<OrderResponse> getAllOrders(int page, int size);

    OrderResponse getOrderByPublicId(String orderPublicId);

    OrderResponse updateOrderStatus(String orderPublicId, UpdateOrderStatusRequest request);
}