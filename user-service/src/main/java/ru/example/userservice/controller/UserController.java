package ru.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.example.userservice.model.request.UpdateCurrentUserRequest;
import ru.example.userservice.model.response.CurrentUserResponse;
import ru.example.userservice.service.CurrentUserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(currentUserService.getCurrentUser(jwt));
    }

    @PatchMapping("/me")
    public ResponseEntity<CurrentUserResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCurrentUserRequest request
    ) {
        return ResponseEntity.ok(currentUserService.updateCurrentUser(jwt, request));
    }
}