package ru.example.productservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.productservice.entity.OutboxEventStatus;
import ru.example.productservice.entity.ProcessedKafkaEvent;
import ru.example.productservice.entity.Product;
import ru.example.productservice.entity.ProductOutboxEvent;
import ru.example.productservice.model.event.StockReservationRequestedEvent;
import ru.example.productservice.model.event.StockReservationResultEvent;
import ru.example.productservice.repository.ProcessedKafkaEventRepository;
import ru.example.productservice.repository.ProductOutboxEventRepository;
import ru.example.productservice.repository.ProductRepository;
import ru.example.productservice.service.StockReservationSagaService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReservationSagaServiceImpl implements StockReservationSagaService {

    private static final String CONSUMER_NAME = "product-service-stock-reservation-consumer";

    private final ProductRepository productRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handleStockReservationRequested(
            UUID sourceEventId,
            StockReservationRequestedEvent event
    ) {
        if (processedKafkaEventRepository.existsByEventIdAndConsumerName(sourceEventId, CONSUMER_NAME)) {
            return;
        }

        StockReservationResultEvent result = tryReserveStock(event);

        ProductOutboxEvent outboxEvent = createResultOutboxEvent(event.orderPublicId(), result);

        productOutboxEventRepository.save(outboxEvent);

        processedKafkaEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(sourceEventId)
                        .consumerName(CONSUMER_NAME)
                        .build()
        );
    }

    private StockReservationResultEvent tryReserveStock(StockReservationRequestedEvent event) {
        Set<String> productPublicIds = event.items()
                .stream()
                .map(StockReservationRequestedEvent.Item::productPublicId)
                .collect(Collectors.toSet());

        List<Product> products = productRepository.findAllByPublicIdInForUpdate(productPublicIds);

        Map<String, Product> productsByPublicId = products.stream()
                .collect(Collectors.toMap(Product::getPublicId, Function.identity()));

        for (StockReservationRequestedEvent.Item item : event.items()) {
            Product product = productsByPublicId.get(item.productPublicId());

            if (product == null) {
                return new StockReservationResultEvent(
                        event.orderPublicId(),
                        false,
                        "Товар productPublicId=" + item.productPublicId() + " не найден"
                );
            }

            if (product.getStockQuantity() == null) {
                return new StockReservationResultEvent(
                        event.orderPublicId(),
                        false,
                        "У товара productPublicId=" + item.productPublicId() + " не задан stockQuantity"
                );
            }

            if (product.getStockQuantity() < item.quantity()) {
                return new StockReservationResultEvent(
                        event.orderPublicId(),
                        false,
                        "Недостаточно товара productPublicId=" + item.productPublicId()
                                + ". Запрошено: " + item.quantity()
                                + ", доступно: " + product.getStockQuantity()
                );
            }
        }

        for (StockReservationRequestedEvent.Item item : event.items()) {
            Product product = productsByPublicId.get(item.productPublicId());
            product.setStockQuantity(product.getStockQuantity() - item.quantity());
        }

        return new StockReservationResultEvent(
                event.orderPublicId(),
                true,
                null
        );
    }

    private ProductOutboxEvent createResultOutboxEvent(
            String orderPublicId,
            StockReservationResultEvent result
    ) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Не удалось сериализовать результат резервирования stock для заказа: "
                            + orderPublicId,
                    exception
            );
        }

        return ProductOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("ORDER")
                .aggregateId(orderPublicId)
                .eventType(result.success()
                        ? "StockReservationSucceeded"
                        : "StockReservationFailed")
                .payload(payloadJson)
                .status(OutboxEventStatus.NEW)
                .attempts(0)
                .build();
    }
}