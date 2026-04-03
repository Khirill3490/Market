package ru.example.authmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("ru.example.identitydomain.entity")
public class AuthModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthModuleApplication.class, args);
    }

}
