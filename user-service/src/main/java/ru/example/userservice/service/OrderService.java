package ru.example.userservice.service;

import org.springframework.security.oauth2.jwt.Jwt;
import ru.example.userservice.model.request.CreateOrderRequest;
import ru.example.userservice.model.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Jwt jwt, CreateOrderRequest request);

    List<OrderResponse> getCurrentUserOrders(Jwt jwt);

    OrderResponse getCurrentUserOrder(Jwt jwt, String orderPublicId);

    OrderResponse cancelCurrentUserOrder(Jwt jwt, String orderPublicId);
}