package ru.example.authmodule.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.example.authmodule.model.request.LoginRequest;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.authmodule.model.request.RefreshTokenRequest;
import ru.example.authmodule.model.request.SimpleResponse;
import ru.example.authmodule.model.response.AuthResponse;
import ru.example.authmodule.model.response.RefreshTokenResponse;
import ru.example.authmodule.security.service.SecurityService;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.UserService;

@RestController
@RequiredArgsConstructor
public class AuthRestController {

    private final SecurityService securityService;
    private final ActivationService activationService;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest loginRequest) {
        System.out.println("В контроллере");
        return ResponseEntity.ok(securityService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> register(@RequestBody UserRegRequest request) {
        securityService.register(request);

        return ResponseEntity.ok(
                new SimpleResponse("На ваш email было отправлено сообщение с ссылкой для подтверждения почты"));
    }

    @GetMapping("/activate")
    public ResponseEntity<SimpleResponse> register(@RequestParam String token) {
        activationService.activate(token);

        return ResponseEntity.ok(new SimpleResponse("Ваш аккаунт успешно активирован"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(securityService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<SimpleResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        securityService.logout();
        return ResponseEntity.ok(new SimpleResponse("Logged out successfully"));
    }
}
