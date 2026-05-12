package ru.example.userservice.exception;

public class ProductOutOfStockException extends RuntimeException {

    public ProductOutOfStockException(String productPublicId, int requestedQuantity, int availableQuantity) {
        super("Недостаточно товара productPublicId=" + productPublicId
                + ". Запрошено: " + requestedQuantity
                + ", доступно: " + availableQuantity);
    }

}