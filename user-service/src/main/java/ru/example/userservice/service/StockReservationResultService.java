package ru.example.userservice.service;

import ru.example.userservice.model.event.StockReservationResultEvent;

import java.util.UUID;

public interface StockReservationResultService {

    void handleStockReservationResult(
            UUID sourceEventId,
            StockReservationResultEvent event
    );
}