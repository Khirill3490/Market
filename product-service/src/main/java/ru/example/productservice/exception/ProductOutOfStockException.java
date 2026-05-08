package ru.example.productservice.exception;

public class ProductOutOfStockException extends RuntimeException {

    public ProductOutOfStockException(
            String productPublicId,
            int requestedQuantity,
            int availableQuantity
    ) {
        super("Недостаточно товара с publicId=" + productPublicId
                + ". Запрошено: " + requestedQuantity
                + ", доступно: " + availableQuantity);
    }
}