package ru.example.userservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.example.userservice.exception.ProductCatalogException;
import ru.example.userservice.model.response.ProductCatalogResponse;

@Component
@RequiredArgsConstructor
public class ProductCatalogClient {

    private final RestClient.Builder restClientBuilder;

    public ProductCatalogResponse getProductByPublicId(String productPublicId) {
        try {
            ProductCatalogResponse response = restClientBuilder
                    .build()
                    .get()
                    .uri("http://product-service/api/v1/products/{productPublicId}", productPublicId)
                    .retrieve()
                    .body(ProductCatalogResponse.class);

            if (response == null) {
                throw new ProductCatalogException(
                        "product-service вернул пустой ответ для товара " + productPublicId
                );
            }

            return response;
        } catch (RestClientException exception) {
            throw new ProductCatalogException(
                    "Не удалось получить данные товара из product-service: " + productPublicId,
                    exception
            );
        }
    }
}