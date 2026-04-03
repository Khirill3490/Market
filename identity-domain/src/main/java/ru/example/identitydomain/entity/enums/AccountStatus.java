package ru.example.identitydomain.entity.enums;

public enum AccountStatus {
    INACTIVE,   // не активирован (ожидает подтверждения почты / проверки)
    ACTIVE,     // активирован
    BLOCKED;    // заблокирован админом
}
