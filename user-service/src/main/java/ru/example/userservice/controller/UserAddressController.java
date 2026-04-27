package ru.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.example.userservice.model.request.CreateAddressRequest;
import ru.example.userservice.model.request.UpdateAddressRequest;
import ru.example.userservice.model.response.AddressResponse;
import ru.example.userservice.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(addressService.getCurrentUserAddresses(jwt));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createMyAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        AddressResponse response = addressService.createAddress(jwt, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{addressPublicId}")
    public ResponseEntity<AddressResponse> updateMyAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String addressPublicId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return ResponseEntity.ok(addressService.updateAddress(jwt, addressPublicId, request));
    }

    @DeleteMapping("/{addressPublicId}")
    public ResponseEntity<Void> deleteMyAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String addressPublicId
    ) {
        addressService.deleteAddress(jwt, addressPublicId);
        return ResponseEntity.noContent().build();
    }
}