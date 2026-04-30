package ru.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.example.userservice.model.request.CreateOrderRequest;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(jwt, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getCurrentUserOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<OrderResponse> response = orderService.getCurrentUserOrders(jwt);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderPublicId}")
    public ResponseEntity<OrderResponse> getCurrentUserOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderPublicId
    ) {
        OrderResponse response = orderService.getCurrentUserOrder(jwt, orderPublicId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderPublicId}/cancel")
    public ResponseEntity<OrderResponse> cancelCurrentUserOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderPublicId
    ) {
        OrderResponse response = orderService.cancelCurrentUserOrder(jwt, orderPublicId);

        return ResponseEntity.ok(response);
    }
}