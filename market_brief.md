# Market Project — актуальный рабочий бриф для нового чата

## 1. Что это за проект

Это Java / Spring Boot multi-module проект **Market**, который нужно постепенно довести до уровня уверенного **production-style backend интернет-магазина**, а не простого учебного CRUD/pet-проекта.

Базовая структура проекта:

- `api-gateway`
- `auth-module`
- `user-service`
- `product-service`
- `eureka-server`
- `common`
- `identity-domain`
- `frontend`
- `docker` / инфраструктурные файлы

Используемый стек:

- Java 21
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Eureka
- Spring Security
- OAuth2 Resource Server
- Keycloak
- PostgreSQL
- JPA / Hibernate
- Flyway
- Gradle multi-module
- Docker Compose
- HTML / JS frontend

Главная цель проекта — не просто “дописать фичи”, а постепенно построить взрослый backend интернет-магазина с понятной архитектурой, security, миграциями, тестами, observability и нормальным roadmap развития.

---

## 2. Роль ChatGPT в работе над проектом

ChatGPT должен выступать не просто как отвечающий помощник, а как **наставник / техлид / reviewer**.

Нужно:

- вести проект по шагам;
- помогать принимать архитектурные решения;
- объяснять, зачем нужна каждая технология и каждый слой;
- подсказывать, где проект уже хорош, а где есть технический долг;
- удерживать движение в сторону production-style backend;
- не прыгать сразу в тяжёлую distributed complexity без крепкого фундамента.

---

## 3. Критически важный формат объяснения кода

Пользователю важно не просто получать готовый код, а **понимать, как он работает в системе**.

Когда ChatGPT предлагает код, класс, сервис, контроллер, DTO, entity, repository, config или миграцию, нужно объяснять не только “что делает код”, но и его место в общей цепочке проекта.

Обязательный формат объяснения:

1. **Где этот класс/метод находится в архитектуре.**
2. **Кто его вызывает.**
3. **На каком этапе request/business flow он срабатывает.**
4. **Зачем он нужен.**
5. **Что делает внутри по шагам.**
6. **Как связан с другими классами.**
7. **Какие есть нюансы, ограничения и технические долги.**
8. **Как проверить это руками через curl/логи/IDEA.**

Пример желаемого стиля: не просто “`OrderMapper` преобразует `Order` в `OrderResponse`”, а объяснить, что `OrderMapper` вызывается из `OrderServiceImpl` и `AdminOrderServiceImpl` после того, как сервис нашёл или создал заказ; что он отделяет внутреннюю JPA entity от внешнего API-контракта; что благодаря этому controller не отдаёт entity напрямую, а возвращает безопасный DTO.

Главное правило: **не присылать большие куски кода без объяснения их места в общей цепочке работы проекта**.

---

## 4. Принятая архитектурная основа

Главное архитектурное решение:

**Keycloak identity → local Account → optional Company → user/domain/business data**

Принято:

- `Keycloak` отвечает за identity, authentication и credential lifecycle.
- Локальный backend не строит собственный полноценный auth-центр.
- Не развиваем старые `signin / refresh / logout / activate / forgotPassword / resetPassword` как основную auth-модель.
- Email verification, password reset, password lifecycle — зона ответственности Keycloak.
- Backend-сервисы работают как OAuth2 Resource Server и сами валидируют JWT.
- `api-gateway` — входная точка и маршрутизация, но не единственный “охранник”.
- Нельзя строить схему, где gateway проверил токен, а остальные сервисы слепо доверяют заголовкам.

Целевая локальная модель:

- `Account` — локальная доменная сущность пользователя магазина.
- `Company` — отдельная optional бизнес-сущность.
- Обычный customer живёт без `Company`.
- Seller/business flow должен быть отдельным сценарием.

---

## 5. Текущая роль `auth-module`

`auth-module` оставлен как тонкий **identity/account-provisioning слой**.

Его главный публичный endpoint:

```http
GET /api/v1/auth/me
```

Что делает `/auth/me`:

- принимает валидный JWT от Keycloak;
- берёт из JWT `sub`, `email`, `given_name`, `family_name`;
- ищет локальный `Account` по `keycloakUserId`;
- если локального `Account` нет — создаёт shell/local account;
- возвращает current account/identity context.

Что важно:

- отдельный публичный `/me/sync` не нужен;
- provisioning локального `Account` — внутренняя серверная механика;
- `auth-module` не должен превращаться в полноценный локальный auth-сервер;
- `auth-module` не должен дублировать `user-service`.

---

## 6. Текущая роль `user-service`

`user-service` отвечает за пользовательские бизнес-данные приложения, а не за identity/auth.

Его зона ответственности:

- профиль текущего пользователя;
- адреса;
- корзина;
- заказы;
- пользовательские бизнес-сценарии;
- дальше — customer/seller settings, seller/company flow.

Разграничение:

```text
/auth/me     → identity/account context + provisioning
/users/me    → профиль и пользовательские business data
```

Нельзя допускать, чтобы `auth-module` и `user-service` стали двумя одинаковыми сервисами с одинаковым `/me`.

---

## 7. Что уже сделано в `user-service`

### 7.1. Профиль текущего пользователя

Собран flow:

```http
GET   /api/v1/users/me
PATCH /api/v1/users/me
```

`CurrentUserService`:

- извлекает `keycloakUserId` из `Jwt`;
- ищет локальный `Account`;
- отдаёт `CurrentUserResponse`;
- обновляет только профильные поля (`firstName`, `lastName`, `phone`).

### 7.2. Address flow

Собран flow адресов:

```http
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PATCH  /api/v1/users/me/addresses/{addressPublicId}
DELETE /api/v1/users/me/addresses/{addressPublicId}
```

Особенности:

- все операции идут от текущего `Account`;
- адрес ищется по связке `addressPublicId + accountId`, а не просто по `publicId`;
- это защищает от доступа к чужим адресам;
- реализована логика default-адреса.

### 7.3. Cart flow

Собрана корзина:

- `Cart`
- `CartItem`
- `CartService`
- `CartController`
- DTO request/response

Текущая логика корзины:

- по JWT находится текущий `Account`;
- находится или создаётся корзина текущего пользователя;
- товар добавляется в корзину;
- если товар уже есть — увеличивается `quantity`;
- можно изменить количество позиции;
- можно удалить позицию;
- можно очистить корзину.

Важные технические моменты:

- `CartRepository` должен использовать `@EntityGraph(attributePaths = "items")`, чтобы корзина подгружалась вместе с позициями.
- В `Cart.items` должен быть `orphanRemoval = true`, чтобы `cart.getItems().clear()` реально удалял строки `cart_items` из БД.
- `getCurrentUserCart(...)` не должен быть `@Transactional(readOnly = true)`, если внутри вызывается `getOrCreateCart(...)`, потому что первый GET корзины может создать корзину.
- В `AddCartItemRequest` и `UpdateCartItemQuantityRequest` нужно иметь `@NotNull + @Min` для `quantity`.

Cart сейчас развивается в сторону актуальных цен и stock-check через `product-service`.

---

## 8. Что уже сделано в `product-service`

`product-service` приведён ближе к общей архитектурной модели проекта.

Сделано:

- добавлена Spring Security / OAuth2 Resource Server модель;
- сервис сам валидирует JWT от Keycloak;
- admin/write endpoints защищаются ролью `ADMIN`;
- публичные GET endpoints каталога доступны без токена;
- убрана старая кастомная auth-логика (`AuthAspect`, `PreAuthorization`, старые user-service классы внутри product-service);
- `ddl-auto` приведён к `validate`;
- Flyway в `product-service` пока выключен, потому что БД общая и миграции временно централизованы в `auth-module`;
- исправлен `EUREKA_SERVER_URL` на нормальную схему через env;
- добавлен `GlobalExceptionHandler` и единый `ErrorResponse`;
- добавлены доменные exceptions для product/category/brand/duplicates/file processing;
- controller/DTO слой почищен;
- `ProductRequest` валидируется;
- `ProductResponse` заполняется через mapper;
- `Page` serialization стабилизирована через `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`;
- `Product`, `Brand`, `Category` согласованы с миграциями;
- `ProductRepository` использует точный поиск по артикулу, а не `Containing`;
- import/export приведены к более аккуратному состоянию;
- `ProductCatalogClient` в `user-service` должен обращаться к `product-service`, а не читать таблицу products напрямую.

Принцип ownership:

```text
product-service владеет Product / Brand / Category / price / stockQuantity
user-service не должен создавать ProductRepository и лезть напрямую в таблицу products
```

Да, сейчас физически БД одна, но логические границы сервисов нужно соблюдать уже сейчас, чтобы потом проще разделять БД/схемы.

---

## 9. Миграции и текущая стратегия БД

Сейчас используется одна общая dev-БД. Поэтому временно **один владелец миграций** — `auth-module`.

Почему так:

- если каждый сервис со своей папкой Flyway будет смотреть в одну и ту же `flyway_schema_history`, начнутся конфликты;
- пока БД общая, безопаснее держать все текущие миграции в одном месте;
- позже можно будет перейти к разным схемам или отдельным БД.

Текущая стратегия:

```text
Сейчас:
  одна PostgreSQL БД app_db
  миграции в auth-module

Ближайшее будущее:
  одна PostgreSQL, но разные schemas: identity, user_domain, catalog, orders

Позже:
  отдельные БД по сервисам: identity_db, user_db, catalog_db, order_db
```

Файлы миграций уже/должны быть примерно такие:

- `V1__init_account_and_company_schema.sql`
- `V2__create_addresses.sql`
- `V3__create_carts_and_cart_items.sql`
- `V4__create_product_catalog_schema.sql`
- `V5__create_orders.sql`
- `V6__add_price_and_stock_to_products_and_orders.sql`

`V6` должен содержать:

```sql
ALTER TABLE products
    ADD COLUMN price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_stock_quantity_non_negative
    CHECK (stock_quantity >= 0);

ALTER TABLE order_items
    ADD COLUMN unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN total_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00;
```

Если миграции ещё не запускались, их можно редактировать/объединять. Если уже применены — нельзя менять содержимое старых миграций, надо создавать новую.

---

## 10. Orders v1 — что уже сделано

Добавлен базовый order flow.

В `identity-domain` добавлены:

- `Order`
- `OrderItem`
- `OrderStatus`

Заказ строится вокруг текущего `Account` и адреса доставки.

Пользовательский API:

```http
POST  /api/v1/users/me/orders
GET   /api/v1/users/me/orders
GET   /api/v1/users/me/orders/{orderPublicId}
PATCH /api/v1/users/me/orders/{orderPublicId}/cancel
```

Логика создания заказа:

```text
JWT
  → local Account
  → Cart текущего Account
  → проверка, что корзина не пустая
  → Address по addressPublicId + accountId
  → Order
  → OrderItems из CartItems
  → clear Cart
  → OrderResponse
```

Важные решения:

- `OrderItem` не имеет JPA-связи на `Product`;
- вместо этого хранится `productPublicId` и snapshot товара;
- это нужно, чтобы заказ не зависел от текущего состояния товара в каталоге;
- старый заказ должен помнить цену/название/картинку товара на момент оформления.

Orders v1 был проверен end-to-end до внедрения полной price/stock логики:

- заказ создавался из корзины;
- корзина очищалась;
- список заказов возвращал созданный заказ.

---

## 11. Admin Orders — что уже сделано

Принято решение пока **не создавать отдельный `admin-module`**.

Текущий подход:

```text
user-service
  /api/v1/users/me/orders   → пользовательский order flow
  /api/v1/admin/orders      → админский order flow
```

Почему так:

- заказы сейчас физически и логически живут в `user-service`;
- отдельный admin-module пока преждевременен;
- но API уже разделён на user/admin;
- позже admin-module можно будет сделать как orchestration layer, который ходит в разные сервисы через клиентов.

Добавлены:

- `AdminOrderController`
- `AdminOrderService`
- `AdminOrderServiceImpl`
- `UpdateOrderStatusRequest`
- `InvalidOrderStatusTransitionException`

Админский API:

```http
GET   /api/v1/admin/orders?page=0&size=20
GET   /api/v1/admin/orders/{orderPublicId}
PATCH /api/v1/admin/orders/{orderPublicId}/status
```

Добавлена пагинация для admin orders, потому что список всех заказов не должен возвращаться целиком.

Разделение логики:

```text
OrderService       → пользовательские действия с заказами текущего Account
AdminOrderService  → админские действия со всеми заказами
OrderMapper        → общий mapper Order → OrderResponse
```

Статусные переходы пока примерно такие:

```text
CREATED    -> PROCESSING или CANCELLED
PROCESSING -> SHIPPED или CANCELLED
PAID       -> PROCESSING или CANCELLED
SHIPPED    -> DELIVERED
DELIVERED  -> terminal
CANCELLED  -> terminal
```

---

## 12. ProductCatalogClient и service-to-service вызовы

Принято решение: `user-service` не создаёт `ProductRepository` и не читает таблицу `products` напрямую.

Правильный подход:

```text
user-service
  → ProductCatalogClient
  → product-service
  → ProductCatalogResponse
```

Почему:

- `product-service` владеет товаром;
- `user-service` владеет корзиной/заказами;
- даже если БД сейчас одна, логические границы сервисов надо соблюдать;
- в будущем при разделении БД это уменьшит боль миграции.

Добавлен/планируется:

- `RestClientConfiguration` с `@LoadBalanced RestClient.Builder`;
- `ProductCatalogClient`;
- `ProductCatalogResponse` в `user-service`.

`ProductCatalogClient` пока фактически вызывает product by `id`, хотя поле в корзине называется `productPublicId`. Это технический долг: позже лучше добавить настоящий `publicId` в `Product` и искать товары по нему.

На текущем этапе допустимо, что `CartItem.productPublicId` хранит строковое значение product id (`"1"`, `"2"`), но это нужно явно помнить.

---

## 13. Price / Stock — текущее состояние и ближайшая цель

Начат этап добавления цены и остатков.

Цель:

```text
product-service:
  Product.price
  Product.stockQuantity
  ProductRequest.price / stockQuantity
  ProductResponse.price / stockQuantity
  ProductMapper маппит эти поля

user-service:
  ProductCatalogResponse.price / stockQuantity
  OrderItem.unitPrice / totalPrice
  OrderItemResponse.unitPrice / totalPrice
  OrderResponse.totalAmount
  OrderMapper считает totalAmount
  OrderServiceImpl сохраняет price snapshot при создании заказа
  CartResponse показывает актуальные цены и totalAmount
  CartService проверяет stockQuantity при add/update item
```

В `product-service` пользователь уже сообщил, что сделал изменения по `Product`, `ProductRequest`, `ProductResponse`, `ProductMapper` для price/stock.

`DataGenerationService` пока специально не трогался. Решение: не переделывать генератор на каждом маленьком изменении модели, а перед запуском/проверкой один раз привести его к актуальной модели (`price`, `stockQuantity`).

Перед проверкой `/gen/{count}` обязательно обновить `DataGenerationService`, иначе он может создавать `Product` с `price = null` и `stockQuantity = null`.

---

## 14. Cart с ценами и stock-check — ближайшая работа

Следующий логичный блок — довести корзину до взрослого состояния.

Целевая логика `GET /api/v1/users/me/cart`:

```json
{
  "publicId": "...",
  "items": [
    {
      "publicId": "...",
      "productPublicId": "1",
      "productName": "Sony Товар 1 Pro",
      "productImage": "https://...",
      "quantity": 2,
      "unitPrice": 1999.99,
      "totalPrice": 3999.98
    }
  ],
  "totalItems": 2,
  "totalAmount": 3999.98
}
```

Важная логика:

- корзина показывает **актуальную** цену из `product-service`;
- заказ хранит **snapshot** цены в `order_items`;
- `CartItem` цену в БД пока не хранит;
- `OrderItem` цену в БД хранит.

Почему:

```text
Cart:
  товар может лежать в корзине долго
  цена может измениться
  корзина должна показать актуальную цену

Order:
  заказ оформлен
  цена должна сохраниться навсегда как snapshot
```

Stock-check в cart:

- при добавлении товара в корзину нужно спросить `product-service`;
- получить `stockQuantity`;
- если итоговое количество в корзине больше остатка — бросить `ProductOutOfStockException`;
- при update quantity тоже проверять `stockQuantity`.

Пока stock только проверяем. Не списываем остатки при добавлении в корзину.

Правильная взрослая логика позже:

```text
cart:
  информативно проверяет доступность

order:
  финально проверяет доступность
  после успешного оформления инициирует списание stock

product-service:
  владеет списанием stock
```

---

## 15. Что пока не нужно делать слишком рано

Пока не надо без необходимости добавлять:

- Kafka;
- Saga;
- Kubernetes;
- Vault;
- service mesh;
- event-driven всё подряд;
- сложное резервирование склада;
- отдельный `admin-module`;
- отдельный `inventory-service`.

Сначала нужен крепкий фундамент:

```text
identity/account
profile
addresses
cart
orders
admin orders
price
stock check
product ownership
seller/company flow
tests
observability
CI/CD
```

---

## 16. Текущий roadmap от текущей точки

Ближайший порядок работ:

1. Довести price/stock до консистентного состояния во всех слоях.
2. Обновить `OrderItem`, `OrderResponse`, `OrderMapper`, `OrderServiceImpl` для price snapshot.
3. Обновить `CartResponse` и `CartServiceImpl`, чтобы корзина показывала актуальные цены и totalAmount.
4. Добавить `ProductOutOfStockException` и stock-check в cart add/update.
5. Добавить финальную stock-проверку при создании заказа.
6. Перед запуском обновить `DataGenerationService` под `price` и `stockQuantity`.
7. Запустить миграции, сервисы и проверить полный flow через curl.
8. После стабилизации перейти к product `publicId`, stock decrement, seller/company ownership.
9. Потом — тесты, observability, CI/CD, Keycloak realm export/import.

---

## 17. Проверенные локальные моменты

- IDEA была переустановлена, настройки в целом подтянулись.
- Проект запускается.
- Пользователь подтвердил, что всё нужное в IDEA/окружении снова работает.
- Keycloak был проверен: realm `market` существует, `.well-known/openid-configuration` отдаёт `200 OK`.
- Была проблема с пользователем `user`: Keycloak отвечал `Account is not fully set up`; рабочим оказался `testuser` с ролью `ADMIN`.
- Для admin-запросов можно использовать `testuser` с `ADMIN`.
- `auth-module`, `user-service`, `product-service`, gateway и Eureka уже поднимались и проверялись в ручных сценариях.

---

## 18. Короткая формулировка текущего состояния

Market — это multi-module Spring Boot проект интернет-магазина. Архитектурно принято, что Keycloak отвечает за identity/auth, backend-сервисы работают как OAuth2 Resource Server, gateway является входной точкой, но не единственной security-границей. Локальная доменная модель строится вокруг `Account + optional Company`. `auth-module` отвечает за identity/account provisioning через `/api/v1/auth/me`. `user-service` отвечает за профиль, адреса, корзину и заказы. `product-service` отвечает за каталог товаров, цену и остатки.

На текущем этапе уже построены и частично проверены profile/address/cart/orders/admin-orders flows. Сейчас проект находится на этапе доведения price/stock логики: товар получает `price` и `stockQuantity`, заказ должен сохранять price snapshot, корзина должна показывать актуальные цены и проверять остатки через `product-service`.

Главное правило работы: идти маленькими шагами, писать production-style код, не просто присылать классы, а объяснять их место в request/business flow и связи с остальными частями проекта.
