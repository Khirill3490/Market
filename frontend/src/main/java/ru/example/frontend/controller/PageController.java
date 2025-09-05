package ru.example.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }

    @GetMapping("/activation")
    public String redirectToActivationPage() {
        return "forward:/activation.html";
    }

    @GetMapping("/account")
    public String account() {
        return "forward:/account.html";
    }

    @GetMapping("/catalog")
    public String catalog() {
        return "forward:/catalog.html";
    }

    @GetMapping("/product")
    public String product() {
        return "forward:/product.html";
    }
}
