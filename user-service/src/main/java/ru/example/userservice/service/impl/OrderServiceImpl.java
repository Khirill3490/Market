package ru.example.userservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.*;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.client.ProductCatalogClient;
import ru.example.userservice.entity.OrderOutboxEvent;
import ru.example.userservice.entity.OutboxEventStatus;
import ru.example.userservice.exception.CartIsEmptyException;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.OrderCannotBeCancelledException;
import ru.example.userservice.exception.OrderNotFoundException;
import ru.example.userservice.mapper.OrderMapper;
import ru.example.userservice.model.event.OrderCancellationRequestedEvent;
import ru.example.userservice.model.event.StockReservationRequestedEvent;
import ru.example.userservice.model.request.CreateOrderRequest;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.model.response.ProductCatalogResponse;
import ru.example.userservice.repository.*;
import ru.example.userservice.service.OrderService;
import ru.example.userservice.service.OrderStatusTransitionService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ProductCatalogClient productCatalogClient;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;
    private final OrderStatusTransitionService orderStatusTransitionService;

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
                .orElseThrow(() -> new EntityNotFoundException("Адрес доставки не найден"));

        Order order = Order.builder()
                .account(account)
                .deliveryAddress(deliveryAddress)
                .status(OrderStatus.PENDING_STOCK_RESERVATION)
                .build();

        List<StockReservationRequestedEvent.Item> reservationItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            ProductCatalogResponse product = productCatalogClient
                    .getProductByPublicId(cartItem.getProductPublicId());


            BigDecimal unitPrice = getRequiredPrice(product);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productPublicId(cartItem.getProductPublicId())
                    .productName(product.name())
                    .productImage(product.img())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            order.addItem(orderItem);

            reservationItems.add(new StockReservationRequestedEvent.Item(
                    cartItem.getProductPublicId(),
                    cartItem.getQuantity()
            ));
        }

        Order savedOrder = orderRepository.save(order);

        orderStatusTransitionService.recordInitialStatus(savedOrder);

        OrderOutboxEvent outboxEvent = createStockReservationRequestedOutboxEvent(
                savedOrder,
                reservationItems
        );

        orderOutboxEventRepository.save(outboxEvent);

        cart.getItems().clear();

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getCurrentUserOrders(Jwt jwt) {
        Account account = getCurrentAccount(jwt);

        return orderRepository
                .findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getCurrentUserOrder(Jwt jwt, String orderPublicId) {
        Account account = getCurrentAccount(jwt);

        Order order = orderRepository
                .findByPublicIdAndAccountId(orderPublicId, account.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderPublicId));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelCurrentUserOrder(Jwt jwt, String orderPublicId) {
        Account account = getCurrentAccount(jwt);

        Order order = orderRepository
                .findByPublicIdAndAccountId(orderPublicId, account.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderPublicId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderCannotBeCancelledException(orderPublicId, order.getStatus());
        }

        orderStatusTransitionService.changeStatus(
                order,
                OrderStatus.CANCELLATION_REQUESTED,
                "Пользователь запросил отмену заказа"
        );

        OrderOutboxEvent outboxEvent =
                createOrderCancellationRequestedOutboxEvent(order);

        orderOutboxEventRepository.save(outboxEvent);

        return orderMapper.toResponse(order);
    }

    private BigDecimal getRequiredPrice(ProductCatalogResponse product) {
        if (product.price() == null) {
            throw new IllegalStateException("product-service вернул товар без цены: " + product.publicId());
        }

        return product.price();
    }


    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Локальный Account для текущего пользователя не найден"
                ));
    }

    private OrderOutboxEvent createStockReservationRequestedOutboxEvent(
            Order order,
            List<StockReservationRequestedEvent.Item> reservationItems
    ) {
        StockReservationRequestedEvent payload = new StockReservationRequestedEvent(
                order.getPublicId(),
                reservationItems
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Не удалось сериализовать событие резервирования stock для заказа: "
                            + order.getPublicId(),
                    exception
            );
        }

        return OrderOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("ORDER")
                .aggregateId(order.getPublicId())
                .eventType("StockReservationRequested")
                .payload(payloadJson)
                .status(OutboxEventStatus.NEW)
                .attempts(0)
                .build();
    }

    private OrderOutboxEvent createOrderCancellationRequestedOutboxEvent(Order order) {
        OrderCancellationRequestedEvent payload =
                new OrderCancellationRequestedEvent(order.getPublicId());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Не удалось сериализовать событие запроса отмены заказа: "
                            + order.getPublicId(),
                    exception
            );
        }

        return OrderOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("ORDER")
                .aggregateId(order.getPublicId())
                .eventType("OrderCancellationRequested")
                .payload(payloadJson)
                .status(OutboxEventStatus.NEW)
                .attempts(0)
                .build();
    }
}