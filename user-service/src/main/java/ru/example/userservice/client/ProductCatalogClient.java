package ru.example.userservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.ProductCatalogException;
import ru.example.userservice.exception.ProductOutOfStockException;
import ru.example.userservice.model.request.DecreaseStockRequest;
import ru.example.userservice.model.response.ProductCatalogResponse;

@Component
@RequiredArgsConstructor
public class ProductCatalogClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public ProductCatalogResponse getProductByPublicId(String productPublicId) {
        try {
            ProductCatalogResponse response = restClientBuilder
                    .build()
                    .get()
                    .uri("http://product-service/api/v1/products/public/{productPublicId}", productPublicId)
                    .retrieve()
                    .body(ProductCatalogResponse.class);

            if (response == null) {
                throw new ProductCatalogException(
                        "product-service вернул пустой ответ для товара " + productPublicId
                );
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw mapGetProductException(productPublicId, exception);
        } catch (RestClientException exception) {
            throw new ProductCatalogException(
                    "Не удалось получить данные товара из product-service: " + productPublicId,
                    exception
            );
        }
    }

    public ProductCatalogResponse decreaseStock(
            String productPublicId,
            int quantity,
            Jwt jwt
    ) {
        try {
            ProductCatalogResponse response = restClientBuilder
                    .build()
                    .patch()
                    .uri(
                            "http://product-service/api/v1/products/public/{productPublicId}/stock/decrease",
                            productPublicId
                    )
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .body(new DecreaseStockRequest(quantity))
                    .retrieve()
                    .body(ProductCatalogResponse.class);

            if (response == null) {
                throw new ProductCatalogException(
                        "product-service вернул пустой ответ при списании stock для товара: "
                                + productPublicId
                );
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw mapDecreaseStockException(productPublicId, quantity, exception);
        } catch (RestClientException exception) {
            throw new ProductCatalogException(
                    "Не удалось списать stock в product-service для товара: " + productPublicId,
                    exception
            );
        }
    }

    private RuntimeException mapGetProductException(
            String productPublicId,
            RestClientResponseException exception
    ) {
        int status = exception.getStatusCode().value();
        String message = extractMessage(exception);

        if (status == 404) {
            return new EntityNotFoundException(
                    "Товар productPublicId=" + productPublicId + " не найден"
            );
        }

        if (status == 401 || status == 403) {
            return new ProductCatalogException(
                    "product-service отказал в доступе при получении товара: " + productPublicId,
                    exception
            );
        }

        return new ProductCatalogException(
                "product-service вернул ошибку при получении товара: " + message,
                exception
        );
    }

    private RuntimeException mapDecreaseStockException(
            String productPublicId,
            int quantity,
            RestClientResponseException exception
    ) {
        int status = exception.getStatusCode().value();
        String message = extractMessage(exception);

        if (status == 404) {
            return new EntityNotFoundException(
                    "Товар productPublicId=" + productPublicId + " не найден"
            );
        }

        if (status == 409) {
            return new ProductOutOfStockException(message);
        }

        if (status == 401 || status == 403) {
            return new ProductCatalogException(
                    "product-service отказал в доступе при списании stock товара: " + productPublicId,
                    exception
            );
        }

        return new ProductCatalogException(
                "product-service вернул ошибку при списании stock товара "
                        + productPublicId
                        + " в количестве "
                        + quantity
                        + ": "
                        + message,
                exception
        );
    }

    private String extractMessage(RestClientResponseException exception) {
        try {
            ProductServiceErrorResponse response = objectMapper.readValue(
                    exception.getResponseBodyAsString(),
                    ProductServiceErrorResponse.class
            );

            if (response.message() != null && !response.message().isBlank()) {
                return response.message();
            }
        } catch (Exception ignored) {
            // Если тело ответа не удалось распарсить, вернём fallback ниже.
        }

        return exception.getResponseBodyAsString();
    }

    private record ProductServiceErrorResponse(
            String message
    ) {
    }
}