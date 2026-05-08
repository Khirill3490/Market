package ru.example.productservice.entity;

public enum OutboxEventStatus {
    NEW,
    PUBLISHED,
    FAILED
}