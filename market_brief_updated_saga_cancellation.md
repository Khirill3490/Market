# Market Project — актуальный рабочий бриф для нового чата

> **Контекст:** это рабочий бриф по проекту **Market** после большого этапа перехода на **Saga + Outbox + Kafka**.  
> Его нужно передать в новый чат **вместе с актуальным архивом проекта**, чтобы продолжить работу без потери контекста.

---

# 1. Что это за проект

**Market** — Java / Spring Boot multi-module проект интернет-магазина, который постепенно доводится до уровня **production-style backend**, а не простого CRUD/pet-проекта.

Проект используется не только для реализации функциональности, но и для **глубокого изучения современной backend-архитектуры**:

- микросервисы;
- Keycloak / OAuth2 / JWT;
- service-to-service взаимодействие;
- Kafka;
- Saga;
- Outbox pattern;
- идемпотентность consumers;
- управление stock/reservations;
- транзакции;
- миграции;
- eventual consistency;
- последующие retry / DLT / integration testing.

---

# 2. Текущая рабочая ветка

Работа по Saga / Outbox / Kafka ведётся в отдельной ветке:

```text
dev_saga_outbox_kafka
```

В новый чат нужно передать именно актуальный проект из этой ветки.

---

# 3. Базовая структура проекта

Основные модули:

- `api-gateway`
- `auth-module`
- `user-service`
- `product-service`
- `eureka-server`
- `identity-domain`
- `docker`
- `scripts`
- `frontend` — локально может оставаться, но пользователь хотел исключить его из GitHub через `.gitignore`

Также пользователь хотел убрать из GitHub:

```gitignore
.idea/
frontend/
```

Если директории уже были в индексе Git, их нужно было убрать через:

```bash
git rm -r --cached .idea
git rm -r --cached frontend
```

---

# 4. Используемый стек

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
- Apache Kafka
- IntelliJ HTTP Client `.http` файлы
- `curl + jq` для ручной проверки

---

# 5. Роль ChatGPT в работе над проектом

ChatGPT должен выступать как:

- наставник;
- техлид;
- reviewer;
- помощник по архитектуре;
- объясняющий партнёр, а не просто генератор кода.

Работать нужно:

- по шагам;
- без хаотичных скачков;
- с пояснением, зачем нужен каждый новый слой;
- с вниманием к уже написанному коду;
- с учетом того, что проект пишется как **взрослый production-style backend**.

---

# 6. Критически важный формат объяснения нового кода

Пользователь отдельно попросил **по умолчанию** объяснять новый код **как бизнес-flow**, а не просто присылать классы и фрагменты.

При объяснении нового кода нужно обязательно отвечать на 10 вопросов:

1. **Зачем этот кусок нужен в бизнес-flow.**
2. **Кто его вызывает.**
3. **Что приходит на вход.**
4. **Какие таблицы читаются.**
5. **Какие таблицы меняются.**
6. **Какие события создаются.**
7. **Где начинается и заканчивается транзакция.**
8. **Что происходит при успехе.**
9. **Что происходит при ошибке.**
10. **Как этот кусок связан с остальной Saga.**

Пользователю особенно полезны:

- подробные текстовые схемы прохождения данных;
- повторное объяснение одной и той же технологии на разных этапах;
- схемы `endpoint → service → repository → DB → outbox → Kafka → consumer`;
- объяснение, что было раньше и что изменилось теперь;
- не построчные комментарии к коду по умолчанию, а **логическая архитектурная раскладка**.

Пример желаемого формата:

```text
Пользователь
    |
    | DELETE /api/v1/users/me/orders/{orderPublicId}
    v
user-service
    |
    | 1. SELECT orders
    | 2. CONFIRMED → CANCELLATION_REQUESTED
    | 3. order_status_history INSERT
    | 4. order_outbox_events INSERT:
    |    OrderCancellationRequested, status=NEW
    | 5. COMMIT
    v
OrderOutboxPublisher
    |
    | Kafka:
    | market.order.cancellation-requested.v1
    v
product-service
    |
    | consumer → saga service
    | stock_reservations → RELEASED
    | products.stock_quantity увеличивается
    | product_outbox_events INSERT
```

Главное правило:  
**не присылать большие куски кода без объяснения их места в общем business-flow проекта.**

---

# 7. Архитектурная основа identity/auth

Принятое решение:

```text
Keycloak identity
    ↓
local Account
    ↓
optional Company
    ↓
business-domain приложения
```

Принято:

- `Keycloak` отвечает за identity, authentication и credential lifecycle.
- Локальный backend не строит собственный полноценный auth-центр.
- Не развиваем старые локальные auth-flow как основную модель:
  - `signin`
  - `refresh`
  - `logout`
  - `activate`
  - `forgotPassword`
  - `resetPassword`
- Email verification / password reset / password lifecycle — зона ответственности Keycloak.
- Backend-сервисы работают как OAuth2 Resource Server и **сами валидируют JWT**.
- `api-gateway` — входная точка и маршрутизация, но не единственная security-граница.
- Нельзя строить схему, где gateway проверил JWT, а downstream-сервисы слепо доверяют заголовкам.

Локальная модель:

- `Account` — локальная бизнес-сущность пользователя.
- `Company` — optional сущность для будущего seller/business flow.

---

# 8. Роль `auth-module`

`auth-module` остаётся тонким **identity/account provisioning слоем**.

Основной endpoint:

```http
GET /api/v1/auth/me
```

Что делает:

- принимает валидный JWT от Keycloak;
- берёт из JWT:
  - `sub`
  - `email`
  - `given_name`
  - `family_name`
- ищет локальный `Account` по `keycloakUserId`;
- если account отсутствует — создаёт shell/local account;
- возвращает identity/account context.

Важно:

- отдельный публичный `/me/sync` не нужен;
- provisioning локального account — внутренняя серверная механика;
- `auth-module` не должен превращаться в полноценный auth-сервер;
- `auth-module` не должен дублировать `user-service`.

---

# 9. Роль `user-service`

`user-service` отвечает за:

- профиль текущего пользователя;
- адреса;
- корзину;
- заказы;
- order status flow;
- consumer-обработку событий от `product-service`;
- Saga-состояние заказа.

Разграничение:

```text
/auth/me     → identity/account context + provisioning
/users/me    → профиль и пользовательские business data
```

---

# 10. Роль `product-service`

`product-service` отвечает за:

- Product / Brand / Category;
- price;
- stockQuantity;
- stock reservation;
- stock release;
- таблицу `stock_reservations`;
- consumer-side processing событий от `user-service`;
- product-side outbox events.

Ownership:

```text
product-service владеет:
  Product / Brand / Category / price / stockQuantity / stock_reservations

user-service владеет:
  Account-scoped cart / orders / order status / order history
```

Да, сейчас dev-БД одна, но логические границы сервисов соблюдаются уже сейчас.

---

# 11. Миграции и текущая стратегия БД

Сейчас используется одна общая dev-БД:

```text
app_db
```

Пока принят временный подход:

```text
единственный владелец Flyway migrations — auth-module
```

Почему:

- все сервисы пока смотрят в одну БД;
- разные Flyway migration folders с общей `flyway_schema_history` создавали бы конфликты;
- позже можно перейти к schemas или отдельным БД.

План взросления:

```text
Сейчас:
  одна PostgreSQL БД app_db
  миграции в auth-module

Позже:
  разные PostgreSQL schemas по зонам ответственности

Ещё позже:
  отдельные БД по сервисам
```

---

# 12. Что уже сделано до Saga

## 12.1. Profile flow

Собран flow:

```http
GET   /api/v1/users/me
PATCH /api/v1/users/me
```

Логика:

- из JWT берётся `keycloakUserId`;
- по нему находится `Account`;
- отдаётся `CurrentUserResponse`;
- можно обновить профильные поля.

---

## 12.2. Address flow

Собран flow:

```http
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PATCH  /api/v1/users/me/addresses/{addressPublicId}
DELETE /api/v1/users/me/addresses/{addressPublicId}
```

Важно:

- address ownership проверяется через `addressPublicId + accountId`;
- реализована default-address логика.

---

## 12.3. Cart flow

Собрана корзина:

- `Cart`
- `CartItem`
- `CartService`
- `CartController`
- DTO request/response.

Важные моменты:

- `CartRepository` использует `@EntityGraph(attributePaths = "items")`;
- `Cart.items` должен иметь `orphanRemoval = true`;
- первый GET корзины не должен быть `readOnly=true`, если при первом запросе корзина создаётся;
- quantity в request DTO валидируется через `@NotNull` / `@Min`.

---

# 13. Product catalog / generator / convenience tooling

В `product-service`:

- публичные GET endpoints каталога;
- write/admin endpoints защищены ролью `ADMIN`;
- есть dev-generator товаров через endpoint:

```http
POST /api/v1/products/gen/{count}
```

Генератор был обновлён так, чтобы создавать товары с:

- `price > 0`;
- `stockQuantity > 0`;
- валидным `publicId`;
- брендом;
- категорией.

Это нужно для удобных ручных Saga-тестов.

Также подготовлен `.http` сценарий для IntelliJ HTTP Client, который использовался для проверки:

- получения user/admin token;
- генерации товара;
- адресов;
- корзины;
- создания заказов;
- failure flow.

Русская версия файла создавалась как:

```text
order-saga-flow-ru.http
```

---

# 14. От старого sync-flow к Saga-flow

Раньше заказ развивался через более синхронную схему:

```text
user-service
  → ProductCatalogClient
  → product-service REST decrease stock
```

После этого проект был переведён на:

```text
Saga + Outbox + Kafka
```

### Что важно

Из `OrderServiceImpl.createOrder(...)` была убрана старая финальная синхронная проверка stock, которая мешала событийному failure flow.

Теперь:

- `user-service` может читать товар для snapshot заказа;
- но **финальное решение**, хватает stock или нет, принимает `product-service` при обработке Kafka-события.

Также из `ProductCatalogClient` был удалён старый REST-вызов:

```text
decreaseStock(...)
```

Потому что списание stock теперь происходит не прямым HTTP-запросом из user-service, а через Saga.

---

# 15. Основной Order Saga-flow: создание заказа

## 15.1. Пользователь создаёт заказ

```http
POST /api/v1/users/me/orders
```

### Бизнес-flow

```text
Пользователь
    |
    | POST /api/v1/users/me/orders
    v
OrderController
    |
    v
OrderServiceImpl.createOrder(...)
    |
    | В одной транзакции user-service:
    |
    | 1. SELECT Account по JWT
    | 2. SELECT Cart текущего Account
    | 3. Проверка, что корзина не пустая
    | 4. SELECT Address по addressPublicId + accountId
    | 5. Создание Order:
    |    status = PENDING_STOCK_RESERVATION
    | 6. Создание OrderItems из CartItems
    |    с snapshot:
    |    - productPublicId
    |    - productName
    |    - productImage
    |    - unitPrice
    |    - totalPrice
    | 7. INSERT orders
    | 8. INSERT order_items
    | 9. INSERT order_status_history:
    |    null → PENDING_STOCK_RESERVATION
    | 10. INSERT order_outbox_events:
    |     StockReservationRequested, status=NEW
    | 11. Очистка cart_items
    |
    | COMMIT
    v
HTTP response:
    status = PENDING_STOCK_RESERVATION
```

---

## 15.2. `OrderOutboxPublisher`

Publisher читает:

```text
order_outbox_events
WHERE status = NEW
```

и публикует в Kafka:

```text
StockReservationRequested
→ market.order.stock-reservation-requested.v1
```

После успеха:

```text
order_outbox_events.status:
NEW → PUBLISHED
```

---

## 15.3. `product-service` обрабатывает stock reservation

Событие принимает:

```text
StockReservationRequestedConsumer
```

Далее вызывается:

```text
StockReservationSagaServiceImpl
```

### Успешный путь

```text
Kafka event:
StockReservationRequested(orderPublicId, items)
        |
        v
StockReservationSagaServiceImpl
        |
        | В одной транзакции product-service:
        |
        | 1. SELECT processed_kafka_events
        |    Проверяем, не обрабатывали ли eventId раньше
        |
        | 2. Агрегируем items по productPublicId
        |
        | 3. SELECT products ... FOR UPDATE
        |    Лочим строки товаров
        |
        | 4. Проверяем:
        |    - товар существует
        |    - stockQuantity не null
        |    - stock хватает
        |
        | 5. products.stock_quantity -= requestedQuantity
        |
        | 6. INSERT stock_reservations:
        |    order_public_id
        |    product_public_id
        |    quantity
        |    status = RESERVED
        |
        | 7. INSERT product_outbox_events:
        |    StockReservationSucceeded, status=NEW
        |
        | 8. INSERT processed_kafka_events
        |
        | COMMIT
```

### Failure path

Если stock не хватает:

```text
products.stock_quantity НЕ меняется
stock_reservations НЕ создаются
product_outbox_events:
  StockReservationFailed, status=NEW
processed_kafka_events:
  INSERT
```

Причина формируется человекочитаемо, например:

```text
Недостаточно товара «Тестовый товар Saga 1 Lite».
Запрошено: 10, доступно: 9
```

---

## 15.4. `ProductOutboxPublisher`

Отправляет:

```text
StockReservationSucceeded / StockReservationFailed
→ market.product.stock-reservation-result.v1
```

После публикации:

```text
product_outbox_events.status:
NEW → PUBLISHED
```

---

## 15.5. `user-service` принимает результат reservation

Событие получает:

```text
StockReservationResultConsumer
```

Далее:

```text
StockReservationResultServiceImpl
```

### При success = true

```text
orders.status:
PENDING_STOCK_RESERVATION → CONFIRMED

orders.status_reason:
null

order_status_history:
PENDING_STOCK_RESERVATION → CONFIRMED
```

### При success = false

```text
orders.status:
PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED

orders.status_reason:
reason из события

order_status_history:
PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED
```

В одной транзакции:

```text
1. SELECT processed_kafka_events
2. SELECT orders по publicId
3. Проверка текущего статуса
4. UPDATE orders.status
5. UPDATE orders.status_reason
6. INSERT order_status_history
7. INSERT processed_kafka_events
8. COMMIT
```

---

# 16. Что уже проверено руками end-to-end

## 16.1. Успешный сценарий создания заказа

Проверено:

```text
stock хватал
→ order стал CONFIRMED
→ stock уменьшился
→ order_outbox_events = PUBLISHED
→ product_outbox_events = PUBLISHED
→ processed_kafka_events содержит записи product-service и user-service
```

---

## 16.2. Отказной сценарий создания заказа

Проверено:

```text
в корзине quantity больше, чем stock на момент Saga
→ order сначала создаётся как PENDING_STOCK_RESERVATION
→ product-service формирует StockReservationFailed
→ user-service переводит заказ в STOCK_RESERVATION_FAILED
→ stock НЕ уменьшается повторно
```

---

# 17. `statusReason` в `Order`

Добавлено поле:

```text
orders.status_reason
```

Смысл:

- хранить причину текущего специального/ошибочного статуса;
- отдавать клиенту понятное объяснение;
- сохранять полезный бизнес-контекст для админки и отладки.

Пример ответа:

```json
{
  "status": "STOCK_RESERVATION_FAILED",
  "statusReason": "Недостаточно товара «Тестовый товар Saga 1 Lite». Запрошено: 10, доступно: 9"
}
```

Код добавлен.

**Но runtime-проверку, что `statusReason` реально сохраняется и возвращается API после последних изменений, нужно выполнить в новом чате.**

---

# 18. `order_status_history`

Добавлена таблица:

```text
order_status_history
```

Она хранит:

- `order_id`
- `from_status`
- `to_status`
- `reason`
- `changed_at`

Добавлен сервис:

```text
OrderStatusTransitionService
```

Он отвечает за:

- смену `orders.status`;
- смену `orders.status_reason`;
- создание записи в `order_status_history`.

Примеры истории:

```text
null → PENDING_STOCK_RESERVATION
PENDING_STOCK_RESERVATION → CONFIRMED
```

или:

```text
null → PENDING_STOCK_RESERVATION
PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED
```

Код добавлен.

**Runtime-проверку появления записей в `order_status_history` нужно выполнить в новом чате.**

---

# 19. `stock_reservations`: настоящий reservation flow

Добавлена таблица:

```text
stock_reservations
```

Она хранит:

- `order_public_id`
- `product_id`
- `product_public_id`
- `quantity`
- `status`
- `created_at`
- `updated_at`

Статусы:

```text
RESERVED
RELEASED
```

### Смысл

Раньше при успешном заказе stock просто уменьшался.  
Product-service не знал:

- под какой заказ ушёл stock;
- какой товар был зарезервирован;
- сколько именно было зарезервировано.

Теперь при успешном резервировании:

```text
products.stock_quantity уменьшается
stock_reservations получает RESERVED запись
```

Это фундамент для Cancellation Saga.

Код добавлен.

**Runtime-проверку появления `stock_reservations` со статусом `RESERVED` нужно выполнить в новом чате.**

---

# 20. Cancellation Saga — код дописан, но ещё не протестирован end-to-end

## 20.1. Инициация отмены в `user-service`

Пользователь отменяет заказ через существующий endpoint отмены, который в проекте реализован как user order cancel flow.

Суть:

```text
Пользователь
    |
    | cancel order request
    v
OrderServiceImpl.cancelCurrentUserOrder(...)
    |
    | В одной транзакции:
    |
    | 1. SELECT Account по JWT
    | 2. SELECT Order по orderPublicId + accountId
    | 3. Проверка:
    |    order.status == CONFIRMED
    |
    | 4. orders:
    |    CONFIRMED → CANCELLATION_REQUESTED
    |
    | 5. orders.status_reason:
    |    "Пользователь запросил отмену заказа"
    |
    | 6. INSERT order_status_history:
    |    CONFIRMED → CANCELLATION_REQUESTED
    |
    | 7. INSERT order_outbox_events:
    |    OrderCancellationRequested, status=NEW
    |
    | COMMIT
```

---

## 20.2. `OrderOutboxPublisher` маршрутизирует разные event types

Теперь publisher не отправляет все события в один topic.

Маршрутизация:

```text
StockReservationRequested
→ market.order.stock-reservation-requested.v1

OrderCancellationRequested
→ market.order.cancellation-requested.v1
```

---

## 20.3. `product-service` получает cancellation command

Событие получает:

```text
OrderCancellationRequestedConsumer
```

Далее вызывается:

```text
StockReleaseSagaServiceImpl
```

### Бизнес-flow

```text
Kafka:
market.order.cancellation-requested.v1
        |
        v
OrderCancellationRequestedConsumer
        |
        v
StockReleaseSagaServiceImpl
        |
        | В одной транзакции:
        |
        | 1. SELECT processed_kafka_events
        |    Проверяем, не обрабатывали ли eventId раньше
        |
        | 2. SELECT stock_reservations
        |    WHERE order_public_id = ...
        |      AND status = RESERVED
        |
        | 3. Если активных reservations нет:
        |    формируем StockReleaseFailed
        |
        | 4. SELECT products ... FOR UPDATE
        |    Лочим товары, по которым освобождаем stock
        |
        | 5. Проверяем, что товары существуют
        |    и stockQuantity не null
        |
        | 6. products.stock_quantity += reservation.quantity
        |
        | 7. stock_reservations:
        |    RESERVED → RELEASED
        |
        | 8. INSERT product_outbox_events:
        |    StockReleaseSucceeded / StockReleaseFailed, status=NEW
        |
        | 9. INSERT processed_kafka_events
        |
        | COMMIT
```

---

## 20.4. `ProductOutboxPublisher` маршрутизирует release-result events

Маршрутизация:

```text
StockReservationSucceeded / StockReservationFailed
→ market.product.stock-reservation-result.v1

StockReleaseSucceeded / StockReleaseFailed
→ market.product.stock-release-result.v1
```

---

## 20.5. `user-service` получает result освобождения stock

Событие получает:

```text
StockReleaseResultConsumer
```

Далее:

```text
StockReleaseResultServiceImpl
```

### Бизнес-flow

```text
Kafka:
market.product.stock-release-result.v1
        |
        v
StockReleaseResultConsumer
        |
        v
StockReleaseResultServiceImpl
        |
        | В одной транзакции:
        |
        | 1. SELECT processed_kafka_events
        |    Проверяем eventId на дубликат
        |
        | 2. SELECT orders
        |    WHERE public_id = event.orderPublicId
        |
        | 3. Проверка:
        |    order.status == CANCELLATION_REQUESTED
        |
        | 4a. Если event.success == true:
        |     orders:
        |     CANCELLATION_REQUESTED → CANCELLED
        |
        |     orders.status_reason:
        |     "Заказ успешно отменён"
        |
        |     INSERT order_status_history:
        |     CANCELLATION_REQUESTED → CANCELLED
        |
        | 4b. Если event.success == false:
        |     orders:
        |     CANCELLATION_REQUESTED → CANCELLATION_FAILED
        |
        |     orders.status_reason:
        |     reason из события
        |
        |     INSERT order_status_history:
        |     CANCELLATION_REQUESTED → CANCELLATION_FAILED
        |
        | 5. INSERT processed_kafka_events
        |
        | COMMIT
```

---

# 21. Какие новые статусы заказа добавлены

В `OrderStatus` добавлены:

```text
PENDING_STOCK_RESERVATION
CONFIRMED
STOCK_RESERVATION_FAILED
CANCELLATION_REQUESTED
CANCELLATION_FAILED
CANCELLED
```

Смысл:

```text
PENDING_STOCK_RESERVATION
  заказ создан, ждём решения product-service

CONFIRMED
  stock успешно зарезервирован

STOCK_RESERVATION_FAILED
  product-service отказал в резервировании

CANCELLATION_REQUESTED
  пользователь запросил отмену, ждём освобождения reservation

CANCELLED
  product-service подтвердил освобождение reservation

CANCELLATION_FAILED
  освобождение reservation не удалось
```

---

# 22. Что нужно сделать ПЕРВЫМ ДЕЛОМ в новом чате

Перед переходом к новому архитектурному этапу нужно **запустить проект и проверить накопленные изменения после последних правок**.

## Обязательная проверка №1 — проект компилируется и стартует

Проверить:

- миграции применяются;
- сервисы поднимаются;
- новые Kafka topics создаются;
- нет compile errors после:
  - `statusReason`
  - `order_status_history`
  - `stock_reservations`
  - Cancellation Saga classes

---

## Обязательная проверка №2 — failure order flow после `statusReason`

Снова создать заказ, который провалится по stock, и убедиться:

```text
orders.status = STOCK_RESERVATION_FAILED
orders.status_reason содержит человекочитаемую причину
GET /orders/{id} возвращает statusReason
order_status_history содержит:
PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED
```

---

## Обязательная проверка №3 — success order flow после `stock_reservations`

Создать успешный заказ и убедиться:

```text
orders.status = CONFIRMED
products.stock_quantity уменьшился
stock_reservations создана:
  status = RESERVED
order_status_history содержит:
  null → PENDING_STOCK_RESERVATION
  PENDING_STOCK_RESERVATION → CONFIRMED
```

---

## Обязательная проверка №4 — happy-path Cancellation Saga

Для заказа в `CONFIRMED`:

1. вызвать отмену;
2. первым ответом получить:
   ```text
   CANCELLATION_REQUESTED
   ```
3. подождать, пока Saga завершится;
4. убедиться:

```text
orders.status = CANCELLED
products.stock_quantity вернулся назад
stock_reservations.status = RELEASED
product_outbox_events содержит StockReleaseSucceeded
order_status_history содержит:
  CONFIRMED → CANCELLATION_REQUESTED
  CANCELLATION_REQUESTED → CANCELLED
```

---

## Опционально позже — failure-path Cancellation Saga

Отдельно можно имитировать:

```text
reservation отсутствует / уже RELEASED
→ StockReleaseFailed
→ order.status = CANCELLATION_FAILED
```

Но это не первый приоритет. Сначала важнее доказать happy-path cancellation.

---

# 23. Следующий архитектурный этап после проверки

Когда проверки выше будут успешны, следующий пункт плана:

```text
6. Усиливаем Kafka consumers через retry / DLT
```

То есть нужно будет сделать:

- общую Kafka error-handling конфигурацию;
- retry для временных ошибок;
- Dead Letter Topic для необрабатываемых сообщений;
- разбор retriable vs non-retriable errors;
- понятное поведение, если consumer получает poison message;
- связь retry/DLT с текущими Saga-flow.

Объяснять этот этап нужно особенно подробно:

- как Kafka считает сообщение обработанным;
- когда коммитится offset;
- что происходит при exception;
- как работает retry;
- когда сообщение уходит в DLT;
- что это значит для Saga;
- как руками разбирать DLT message.

---

# 24. Проверенные ручные сценарии в текущем чате

Уже вручную подтверждено:

## Success stock reservation

```text
order:
  PENDING_STOCK_RESERVATION → CONFIRMED

product stock:
  уменьшился на quantity

outbox:
  order_outbox_events = PUBLISHED
  product_outbox_events = PUBLISHED

processed events:
  product-service consumer записал event
  user-service result consumer записал event
```

## Failed stock reservation

```text
order:
  PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED

product stock:
  не уменьшился повторно при failed reservation
```

---

# 25. Короткое состояние проекта на момент перехода в новый чат

На момент завершения текущего чата проект Market находится в состоянии:

- первая Saga создания заказа через Outbox + Kafka уже **реализована и вручную проверена**;
- отказной stock-reservation сценарий уже **реализован и вручную проверен**;
- `statusReason`, `order_status_history`, `stock_reservations` и полная Cancellation Saga **написаны**, но требуют общего end-to-end запуска после накопленных изменений;
- после проверки следующим крупным этапом должен стать **Kafka retry / DLT**.

---

# 26. Как начать новый чат

В новом чате пользователь должен:

1. загрузить актуальный архив проекта из ветки:
   ```text
   dev_saga_outbox_kafka
   ```
2. загрузить этот обновлённый бриф;
3. сказать, что нужно продолжить с проверки накопленных изменений после Cancellation Saga.

Первый ответ ассистента в новом чате должен быть не новым кодом, а:

- изучить проект;
- сверить код с брифом;
- запустить/провести ревизию логики;
- помочь пошагово проверить:
  - migrations;
  - service startup;
  - statusReason;
  - order_status_history;
  - stock_reservations;
  - happy-path Cancellation Saga.

После этого переходить к:

```text
Kafka retry / DLT
```
