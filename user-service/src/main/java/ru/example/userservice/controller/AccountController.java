package ru.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.common.dto.SimpleMessageResponse;
import ru.example.userservice.model.response.AccountResponse;
import ru.example.userservice.service.AccountService;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getUserAccount(
            @RequestHeader("publicId") String publicId) {
        return ResponseEntity.ok(accountService.getAccountByPublicId(publicId));
    }

    @PostMapping("/me/status/change")
    public ResponseEntity<SimpleMessageResponse> changeUserStatus(
            @RequestHeader("publicId") String publicId) {
        return ResponseEntity.ok(accountService.changeUserStatus(publicId));
    }
}
