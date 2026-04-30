package ru.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.example.userservice.model.request.PageRequestParams;
import ru.example.userservice.model.request.UpdateOrderStatusRequest;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.service.AdminOrderService;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Validated
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @Valid @ModelAttribute PageRequestParams request
    ) {
        Page<OrderResponse> response = adminOrderService.getAllOrders(
                request.getPage(),
                request.getSize()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderPublicId}")
    public ResponseEntity<OrderResponse> getOrderByPublicId(
            @PathVariable String orderPublicId
    ) {
        OrderResponse response = adminOrderService.getOrderByPublicId(orderPublicId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderPublicId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderPublicId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse response = adminOrderService.updateOrderStatus(orderPublicId, request);

        return ResponseEntity.ok(response);
    }
}