package ru.example.userservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.ProductCatalogException;
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