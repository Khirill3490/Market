package ru.example.productservice.service;

import ru.example.productservice.model.event.StockReservationRequestedEvent;

import java.util.UUID;

public interface StockReservationSagaService {

    void handleStockReservationRequested(
            UUID sourceEventId,
            StockReservationRequestedEvent event
    );
}