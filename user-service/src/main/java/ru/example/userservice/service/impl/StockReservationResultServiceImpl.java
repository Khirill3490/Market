package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.entity.ProcessedKafkaEvent;
import ru.example.userservice.exception.OrderNotFoundException;
import ru.example.userservice.model.event.StockReservationResultEvent;
import ru.example.userservice.repository.OrderRepository;
import ru.example.userservice.repository.ProcessedKafkaEventRepository;
import ru.example.userservice.service.StockReservationResultService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReservationResultServiceImpl implements StockReservationResultService {

    private static final String CONSUMER_NAME = "user-service-stock-reservation-result-consumer";

    private final OrderRepository orderRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;

    @Override
    @Transactional
    public void handleStockReservationResult(
            UUID sourceEventId,
            StockReservationResultEvent event
    ) {
        if (processedKafkaEventRepository.existsByEventIdAndConsumerName(sourceEventId, CONSUMER_NAME)) {
            return;
        }

        Order order = orderRepository.findByPublicId(event.orderPublicId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderPublicId()));

        if (order.getStatus() == OrderStatus.PENDING_STOCK_RESERVATION) {
            if (event.success()) {
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                order.setStatus(OrderStatus.STOCK_RESERVATION_FAILED);
            }
        }

        processedKafkaEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(sourceEventId)
                        .consumerName(CONSUMER_NAME)
                        .build()
        );
    }
}