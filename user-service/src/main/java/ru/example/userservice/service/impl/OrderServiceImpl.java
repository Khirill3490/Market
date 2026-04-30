package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.*;
import ru.example.userservice.exception.CartIsEmptyException;
import ru.example.userservice.exception.OrderNotFoundException;
import ru.example.userservice.model.request.CreateOrderRequest;
import ru.example.userservice.model.response.OrderItemResponse;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.repository.AccountRepository;
import ru.example.userservice.repository.AddressRepository;
import ru.example.userservice.repository.CartRepository;
import ru.example.userservice.repository.OrderRepository;
import ru.example.userservice.service.OrderService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(Jwt jwt, CreateOrderRequest request) {
        Account account = getCurrentAccount(jwt);

        Cart cart = cartRepository.findByAccountId(account.getId())
                .orElseThrow(CartIsEmptyException::new);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartIsEmptyException();
        }

        Address deliveryAddress = addressRepository
                .findByPublicIdAndAccountId(request.addressPublicId(), account.getId())
                .orElseThrow(() -> new IllegalArgumentException("Адрес доставки не найден"));

        Order order = Order.builder()
                .account(account)
                .deliveryAddress(deliveryAddress)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productPublicId(cartItem.getProductPublicId())
                    .productName(cartItem.getProductPublicId())
                    .productImage(null)
                    .quantity(cartItem.getQuantity())
                    .build();

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();

        return mapToResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getCurrentUserOrders(Jwt jwt) {
        Account account = getCurrentAccount(jwt);

        return orderRepository
                .findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OrderResponse getCurrentUserOrder(Jwt jwt, String orderPublicId) {
        Account account = getCurrentAccount(jwt);

        Order order = orderRepository
                .findByPublicIdAndAccountId(orderPublicId, account.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderPublicId));

        return mapToResponse(order);
    }

    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Локальный Account для текущего пользователя не найден"
                ));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapItemToResponse)
                .toList();

        int totalItems = items.stream()
                .mapToInt(OrderItemResponse::quantity)
                .sum();

        return new OrderResponse(
                order.getPublicId(),
                order.getStatus(),
                order.getDeliveryAddress().getPublicId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items,
                totalItems
        );
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductPublicId(),
                item.getProductName(),
                item.getProductImage(),
                item.getQuantity()
        );
    }
}