package ru.example.productservice.service;

import ru.example.productservice.model.event.OrderCancellationRequestedEvent;

import java.util.UUID;

public interface StockReleaseSagaService {

    void handleOrderCancellationRequested(
            UUID sourceEventId,
            OrderCancellationRequestedEvent event
    );
}