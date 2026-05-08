package ru.example.userservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.example.userservice.exception.ProductCatalogException;
import ru.example.userservice.model.request.DecreaseStockRequest;
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
                    .uri("http://product-service/api/v1/products/public/{productPublicId}", productPublicId)
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

    public ProductCatalogResponse decreaseStock(String productPublicId, int quantity) {
        ProductCatalogResponse response = restClientBuilder
                .build()
                .patch()
                .uri(
                        "http://product-service/api/v1/products/public/{productPublicId}/stock/decrease",
                        productPublicId
                )
                .body(new DecreaseStockRequest(quantity))
                .retrieve()
                .body(ProductCatalogResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "product-service вернул пустой ответ при списании stock для товара: "
                            + productPublicId
            );
        }

        return response;
    }
}