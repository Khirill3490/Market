package ru.example.userservice.service;

import ru.example.userservice.model.event.StockReleaseResultEvent;

import java.util.UUID;

public interface StockReleaseResultService {

    void handleStockReleaseResult(
            UUID sourceEventId,
            StockReleaseResultEvent event
    );
}