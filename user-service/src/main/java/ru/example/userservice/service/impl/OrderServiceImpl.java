package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.*;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.client.ProductCatalogClient;
import ru.example.userservice.exception.*;
import ru.example.userservice.mapper.OrderMapper;
import ru.example.userservice.model.request.CreateOrderRequest;
import ru.example.userservice.model.response.OrderResponse;
import ru.example.userservice.model.response.ProductCatalogResponse;
import ru.example.userservice.repository.AccountRepository;
import ru.example.userservice.repository.AddressRepository;
import ru.example.userservice.repository.CartRepository;
import ru.example.userservice.repository.OrderRepository;
import ru.example.userservice.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductCatalogClient productCatalogClient;
    private final OrderMapper orderMapper;

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
                .build();

        for (CartItem cartItem : cart.getItems()) {
            ProductCatalogResponse product = productCatalogClient
                    .getProductByPublicId(cartItem.getProductPublicId());

            ensureStockAvailable(
                    product,
                    cartItem.getProductPublicId(),
                    cartItem.getQuantity()
            );

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
        }

        Order savedOrder = orderRepository.save(order);

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

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new OrderCannotBeCancelledException(orderPublicId, order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toResponse(order);
    }

    private BigDecimal getRequiredPrice(ProductCatalogResponse product) {
        if (product.price() == null) {
            throw new IllegalStateException("product-service вернул товар без цены: " + product.publicId());
        }

        return product.price();
    }

    private void ensureStockAvailable(
            ProductCatalogResponse product,
            String productPublicId,
            int requestedQuantity
    ) {
        Integer availableQuantity = product.stockQuantity();

        if (availableQuantity == null) {
            throw new IllegalStateException("product-service вернул товар без stockQuantity: " + product.publicId());
        }

        if (requestedQuantity > availableQuantity) {
            throw new ProductOutOfStockException(
                    productPublicId,
                    requestedQuantity,
                    availableQuantity
            );
        }
    }

    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Локальный Account для текущего пользователя не найден"
                ));
    }
}