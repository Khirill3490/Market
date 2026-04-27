package ru.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.example.userservice.model.request.AddCartItemRequest;
import ru.example.userservice.model.request.UpdateCartItemQuantityRequest;
import ru.example.userservice.model.response.CartResponse;
import ru.example.userservice.service.CartService;

@RestController
@RequestMapping("/api/v1/users/me/cart")
@RequiredArgsConstructor
public class UserCartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(cartService.getCurrentUserCart(jwt));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(jwt, request));
    }

    @PatchMapping("/items/{itemPublicId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String itemPublicId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(jwt, itemPublicId, request)
        );
    }

    @DeleteMapping("/items/{itemPublicId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String itemPublicId
    ) {
        cartService.deleteItem(jwt, itemPublicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearMyCart(
            @AuthenticationPrincipal Jwt jwt
    ) {
        cartService.clearCart(jwt);
        return ResponseEntity.noContent().build();
    }
}