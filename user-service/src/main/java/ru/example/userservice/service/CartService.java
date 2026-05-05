package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.Cart;
import ru.example.identitydomain.entity.CartItem;
import ru.example.userservice.client.ProductCatalogClient;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.IncorrectDataException;
import ru.example.userservice.exception.ProductOutOfStockException;
import ru.example.userservice.model.request.AddCartItemRequest;
import ru.example.userservice.model.request.UpdateCartItemQuantityRequest;
import ru.example.userservice.model.response.CartItemResponse;
import ru.example.userservice.model.response.CartResponse;
import ru.example.userservice.model.response.ProductCatalogResponse;
import ru.example.userservice.repository.AccountRepository;
import ru.example.userservice.repository.CartItemRepository;
import ru.example.userservice.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountRepository accountRepository;
    private final ProductCatalogClient productCatalogClient;

    @Transactional
    public CartResponse getCurrentUserCart(Jwt jwt) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getOrCreateCart(account);

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Jwt jwt, AddCartItemRequest request) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getOrCreateCart(account);

        ProductCatalogResponse product = productCatalogClient
                .getProductByPublicId(request.productPublicId());

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProductPublicId().equals(request.productPublicId()))
                .findFirst()
                .orElse(null);

        int requestedQuantity = request.quantity();

        if (existingItem != null) {
            requestedQuantity = existingItem.getQuantity() + request.quantity();
        }

        ensureStockAvailable(
                product,
                request.productPublicId(),
                requestedQuantity
        );

        if (existingItem != null) {
            existingItem.setQuantity(requestedQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .publicId(generatePublicId())
                    .cart(cart)
                    .productPublicId(request.productPublicId())
                    .quantity(request.quantity())
                    .build();

            cart.getItems().add(newItem);
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(
            Jwt jwt,
            String itemPublicId,
            UpdateCartItemQuantityRequest request
    ) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getExistingCart(account);

        CartItem item = cartItemRepository.findByPublicIdAndCartId(itemPublicId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Позиция корзины текущего пользователя не найдена"
                ));

        ProductCatalogResponse product = productCatalogClient
                .getProductByPublicId(item.getProductPublicId());

        ensureStockAvailable(
                product,
                item.getProductPublicId(),
                request.quantity()
        );

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return mapToCartResponse(cart);
    }

    @Transactional
    public void deleteItem(Jwt jwt, String itemPublicId) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getExistingCart(account);

        CartItem item = cartItemRepository.findByPublicIdAndCartId(itemPublicId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Позиция корзины текущего пользователя не найдена"
                ));

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Jwt jwt) {
        Account account = getCurrentAccount(jwt);

        cartRepository.findByAccountId(account.getId())
                .ifPresent(cart -> {
                    List<CartItem> items = cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId());
                    cartItemRepository.deleteAll(items);
                });
    }

    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Локальный account для текущего пользователя не найден"
                ));
    }

    private Cart getOrCreateCart(Account account) {
        return cartRepository.findByAccountId(account.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .publicId(generatePublicId())
                                .account(account)
                                .build()
                ));
    }

    private Cart getExistingCart(Account account) {
        return cartRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Корзина текущего пользователя не найдена"
                ));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getPublicId(),
                items,
                totalItems,
                totalAmount
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        ProductCatalogResponse product = productCatalogClient
                .getProductByPublicId(item.getProductPublicId());

        BigDecimal unitPrice = getRequiredPrice(product);
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getPublicId(),
                item.getProductPublicId(),
                product.name(),
                product.img(),
                item.getQuantity(),
                unitPrice,
                totalPrice
        );
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }

    private String generatePublicId() {
        return UUID.randomUUID().toString().replace("-", "");
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
}