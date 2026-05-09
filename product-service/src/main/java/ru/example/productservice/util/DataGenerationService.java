package ru.example.productservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.productservice.entity.Brand;
import ru.example.productservice.entity.Category;
import ru.example.productservice.entity.Product;
import ru.example.productservice.repository.BrandRepository;
import ru.example.productservice.repository.CategoryRepository;
import ru.example.productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DataGenerationService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageCatalog imageCatalog;

    @Transactional
    public List<Product> save(int count) {
        List<Brand> brands = ensureDefaultBrands();
        List<Category> categories = ensureDefaultCategories();

        List<Product> products = generateTestProducts(count, brands, categories);

        return productRepository.saveAll(products);
    }

    private List<Product> generateTestProducts(
            int count,
            List<Brand> brands,
            List<Category> categories
    ) {
        List<Product> products = new ArrayList<>();
        long batchId = System.currentTimeMillis();

        for (int i = 1; i <= count; i++) {
            Brand brand = randomItem(brands);
            Category category = randomItem(categories);

            String art = "TEST-SAGA-" + batchId + "-" + i;

            Product product = Product.builder()
                    .art(art)
                    .brand(brand)
                    .name("Тестовый товар Saga " + i + " " + getRandomSuffix())
                    .price(randomPrice())
                    .stockQuantity(randomStockQuantity())
                    .inf("Краткое описание тестового товара " + i)
                    .ext("Подробное описание тестового товара для проверки Saga Outbox Kafka")
                    .img(imageCatalog.random())
                    .url("/product/" + art.toLowerCase())
                    .unit(randomUnit())
                    .sml(getRandomSize())
                    .category(category)
                    .bar(randomBarcode())
                    .build();

            products.add(product);
        }

        return products;
    }

    private List<Brand> ensureDefaultBrands() {
        List<Brand> brands = new ArrayList<>();

        brands.add(getOrCreateBrand("Apple", "Apple Inc", "https://www.apple.com"));
        brands.add(getOrCreateBrand("Samsung", "Samsung Electronics", "https://www.samsung.com"));
        brands.add(getOrCreateBrand("Sony", "Sony Corporation", "https://www.sony.com"));
        brands.add(getOrCreateBrand("HP", "HP, Hewlett Packard", "https://www.hp.com"));
        brands.add(getOrCreateBrand("Lenovo", "Lenovo Group", "https://www.lenovo.com"));

        return brands;
    }

    private Brand getOrCreateBrand(
            String name,
            String psName,
            String url
    ) {
        return brandRepository.findByNameEqualsIgnoreCase(name)
                .orElseGet(() -> {
                    Brand brand = new Brand();
                    brand.setName(name);
                    brand.setPsName(psName);
                    brand.setUrl(url);

                    return brandRepository.save(brand);
                });
    }

    private List<Category> ensureDefaultCategories() {
        List<Category> categories = new ArrayList<>();

        categories.add(getOrCreateCategory("Электроника"));
        categories.add(getOrCreateCategory("Бытовая техника"));
        categories.add(getOrCreateCategory("Одежда"));
        categories.add(getOrCreateCategory("Мебель"));
        categories.add(getOrCreateCategory("Спорт"));
        categories.add(getOrCreateCategory("Книги"));
        categories.add(getOrCreateCategory("Игрушки"));

        return categories;
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);

                    return categoryRepository.save(category);
                });
    }

    private BigDecimal randomPrice() {
        int price = ThreadLocalRandom.current().nextInt(1000, 90_001);

        return BigDecimal.valueOf(price);
    }

    private int randomStockQuantity() {
        return ThreadLocalRandom.current().nextInt(5, 101);
    }

    private String randomUnit() {
        String[] units = {"шт.", "кг", "л", "упак."};

        return units[ThreadLocalRandom.current().nextInt(units.length)];
    }

    private String getRandomSuffix() {
        String[] suffixes = {"Pro", "Lite", "Plus", "Max", "Air", "Gold"};

        return suffixes[ThreadLocalRandom.current().nextInt(suffixes.length)];
    }

    private String getRandomSize() {
        int width = ThreadLocalRandom.current().nextInt(5, 51);
        int height = ThreadLocalRandom.current().nextInt(5, 51);
        int depth = ThreadLocalRandom.current().nextInt(5, 51);

        return width + "x" + height + "x" + depth + " см";
    }

    private String randomBarcode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 13);
    }

    private <T> T randomItem(List<T> items) {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }
}