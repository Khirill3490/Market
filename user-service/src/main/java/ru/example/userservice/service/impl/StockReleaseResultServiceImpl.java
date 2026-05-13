package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Order;
import ru.example.identitydomain.entity.enums.OrderStatus;
import ru.example.userservice.entity.ProcessedKafkaEvent;
import ru.example.userservice.exception.OrderNotFoundException;
import ru.example.userservice.model.event.StockReleaseResultEvent;
import ru.example.userservice.repository.OrderRepository;
import ru.example.userservice.repository.ProcessedKafkaEventRepository;
import ru.example.userservice.service.OrderStatusTransitionService;
import ru.example.userservice.service.StockReleaseResultService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReleaseResultServiceImpl implements StockReleaseResultService {

    private static final String CONSUMER_NAME = "user-service-stock-release-result-consumer";

    private final OrderRepository orderRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final OrderStatusTransitionService orderStatusTransitionService;

    @Override
    @Transactional
    public void handleStockReleaseResult(
            UUID sourceEventId,
            StockReleaseResultEvent event
    ) {
        if (processedKafkaEventRepository.existsByEventIdAndConsumerName(sourceEventId, CONSUMER_NAME)) {
            return;
        }

        Order order = orderRepository.findByPublicId(event.orderPublicId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderPublicId()));

        if (order.getStatus() == OrderStatus.CANCELLATION_REQUESTED) {
            if (event.success()) {
                orderStatusTransitionService.changeStatus(
                        order,
                        OrderStatus.CANCELLED,
                        "Заказ успешно отменён"
                );
            } else {
                orderStatusTransitionService.changeStatus(
                        order,
                        OrderStatus.CANCELLATION_FAILED,
                        resolveFailureReason(event)
                );
            }
        }

        processedKafkaEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(sourceEventId)
                        .consumerName(CONSUMER_NAME)
                        .build()
        );
    }

    private String resolveFailureReason(StockReleaseResultEvent event) {
        if (event.reason() == null || event.reason().isBlank()) {
            return "Не удалось освободить резерв товара при отмене заказа";
        }

        return event.reason();
    }
}