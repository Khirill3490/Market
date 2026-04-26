package ru.example.authmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.example.authmodule.model.response.CurrentAccountResponse;
import ru.example.authmodule.security.service.CurrentAccountService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final CurrentAccountService currentAccountService;

    @GetMapping("/me")
    public ResponseEntity<CurrentAccountResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(currentAccountService.getCurrentAccount(jwt));
    }
}