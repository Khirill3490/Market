package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.Cart;
import ru.example.identitydomain.entity.CartItem;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.IncorrectDataException;
import ru.example.userservice.model.request.AddCartItemRequest;
import ru.example.userservice.model.request.UpdateCartItemQuantityRequest;
import ru.example.userservice.model.response.CartItemResponse;
import ru.example.userservice.model.response.CartResponse;
import ru.example.userservice.repository.AccountRepository;
import ru.example.userservice.repository.CartItemRepository;
import ru.example.userservice.repository.CartRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public CartResponse getCurrentUserCart(Jwt jwt) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getOrCreateCart(account);

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Jwt jwt, AddCartItemRequest request) {
        Account account = getCurrentAccount(jwt);
        Cart cart = getOrCreateCart(account);

        CartItem item = cartItemRepository.findByCartIdAndProductPublicId(
                        cart.getId(),
                        request.productPublicId()
                )
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
                    return existingItem;
                })
                .orElseGet(() -> CartItem.builder()
                        .publicId(generatePublicId())
                        .cart(cart)
                        .productPublicId(request.productPublicId().trim())
                        .quantity(request.quantity())
                        .build()
                );

        cartItemRepository.save(item);

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
        List<CartItemResponse> items = cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        int totalItems = items.stream()
                .map(CartItemResponse::getQuantity)
                .reduce(0, Integer::sum);

        return CartResponse.builder()
                .publicId(cart.getPublicId())
                .items(items)
                .totalItems(totalItems)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .publicId(item.getPublicId())
                .productPublicId(item.getProductPublicId())
                .quantity(item.getQuantity())
                .build();
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
}