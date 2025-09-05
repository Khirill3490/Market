package ru.example.productservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.example.common.entity.Brand;
import ru.example.common.entity.Category;
import ru.example.common.entity.Product;
import ru.example.productservice.repository.BrandRepository;
import ru.example.productservice.repository.CategoryRepository;
import ru.example.productservice.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DataGenerationService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageCatalog imageCatalog;

    public void save(int count) {
        addBrand();
        addCategory();
        productRepository.saveAll(generateTestProducts(count));
    }

    public List<Product> generateTestProducts(int count) {
        List<Product> products = new ArrayList<>();
        List<Brand> brands = brandRepository.findAll();
        List<Category> categories = categoryRepository.findAll(); // Получаем все категории из БД
        String[] units = {"шт.", "кг", "л", "упак."};

        Random random = new Random();

        for (int i = 1; i <= count; i++) {
            Product product = new Product();
            product.setArt("ART-" + (1000 + i));
            product.setBrand(brands.get(i % brands.size()));
            product.setName("Товар " + i + " " + getRandomSuffix());
            product.setInf("Краткое описание товара " + i);
            product.setExt("Подробное описание товара " + i + ". " + getRandomFeatures());
            product.setImg(imageCatalog.random());
            product.setUrl("/product/" + i);
            product.setUnit(units[random.nextInt(units.length)]);
            product.setSml(getRandomSize());
            product.setCategory(categories.get(random.nextInt(categories.size()))); // Устанавливаем случайную категорию
            product.setBar(String.valueOf(100000000000L + i));

            products.add(product);
        }

        return products;
    }

    // Остальные методы остаются без изменений
    private String getRandomSuffix() {
        String[] suffixes = {"Pro", "Lite", "Plus", "Max", "Air", "Gold"};
        return suffixes[new Random().nextInt(suffixes.length)];
    }

    private String getRandomFeatures() {
        String[] features = {"Водонепроницаемый", "Энергосберегающий", "Стильный дизайн",
                "Удобный", "Прочный", "Компактный"};
        return features[new Random().nextInt(features.length)];
    }

    private String getRandomSize() {
        Random r = new Random();
        return r.nextInt(50) + "x" + r.nextInt(50) + "x" + r.nextInt(50) + " см";
    }

    private void addBrand() {
        List<Brand> brands = new ArrayList<>();
        Brand brand1 = new Brand();
        brand1.setName("Apple");
        brand1.setPsName("Apple Inc");
        brand1.setUrl("https://www.apple.com");

        Brand brand2 = new Brand();
        brand2.setName("Samsung");
        brand2.setPsName("Samsung Electronics");
        brand2.setUrl("https://www.samsung.com");

        Brand brand3 = new Brand();
        brand3.setName("Sony");
        brand3.setPsName("Sony Corporation");
        brand3.setUrl("https://www.sony.com");

        Brand brand4 = new Brand();
        brand4.setName("HP");
        brand4.setPsName("HP,Hewlett Packard");
        brand4.setUrl("https://www.hp.com");

        Brand brand5 = new Brand();
        brand5.setName("Lenovo");
        brand5.setPsName("Lenovo Group");
        brand5.setUrl("https://www.lenovo.com");

        brands.add(brand1);
        brands.add(brand2);
        brands.add(brand3);
        brands.add(brand4);
        brands.add(brand5);

        brandRepository.saveAll(brands);


    }

    public void addCategory() {
        List<Category> defaultCategories = new ArrayList<>();

        Category electronics = new Category();
        electronics.setName("Электроника");
        defaultCategories.add(electronics);

        Category appliances = new Category();
        appliances.setName("Бытовая техника");
        defaultCategories.add(appliances);

        Category clothing = new Category();
        clothing.setName("Одежда");
        defaultCategories.add(clothing);

        Category furniture = new Category();
        furniture.setName("Мебель");
        defaultCategories.add(furniture);

        Category sport = new Category();
        sport.setName("Спорт");
        defaultCategories.add(sport);

        Category books = new Category();
        books.setName("Книги");
        defaultCategories.add(books);

        Category toys = new Category();
        toys.setName("Игрушки");
        defaultCategories.add(toys);

        categoryRepository.saveAll(defaultCategories);
    }





}

