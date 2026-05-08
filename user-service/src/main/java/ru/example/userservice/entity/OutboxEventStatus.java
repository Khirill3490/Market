package ru.example.userservice.entity;

public enum OutboxEventStatus {
    NEW,
    PUBLISHED,
    FAILED
}