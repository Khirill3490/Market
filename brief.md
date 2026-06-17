# Market Project — обновлённый бриф для нового чата

> **Контекст:** это актуальный рабочий бриф по проекту **Market** после этапов **Saga + Outbox + Kafka**, проверки cancellation flow, первичного retry/DLT, выравнивания order lifecycle и выбора frontend-шаблона.  
> Этот файл нужно передать в новый чат вместе с актуальным архивом проекта.

---

# 1. Что это за проект

**Market** — Java / Spring Boot multi-module проект интернет-магазина / маркетплейса, который доводится до уровня **production-style backend**, а не простого CRUD-проекта.

Проект используется для глубокого изучения современной backend-архитектуры:

- микросервисы;
- Keycloak / OAuth2 / JWT;
- API Gateway;
- service-to-service взаимодействие;
- Kafka;
- Saga;
- Outbox pattern;
- idempotent consumers;
- retry / DLT;
- управление stock/reservations;
- транзакции;
- миграции;
- eventual consistency;
- frontend integration через API Gateway.

---

# 2. Текущая рабочая ветка

Работа ведётся в ветке:

```text
dev_saga_outbox_kafka
```

В новый чат нужно передать актуальный архив проекта именно из этой ветки.

---

# 3. Базовая структура проекта

Основные модули / директории:

```text
Market/
  api-gateway/
  auth-module/
  user-service/
  product-service/
  eureka-server/
  identity-domain/
  docker/
  scripts/
  frontend/
  Makefile
```

`frontend/` теперь используется как отдельная frontend-директория со статическим HTML/CSS/JS шаблоном.

Важно:

- `frontend` **не является Gradle-модулем**;
- его не нужно добавлять в `settings.gradle`;
- frontend позже можно запускать отдельным Docker/nginx-сервисом;
- пока для разработки frontend запускается простой командой через `python3 -m http.server 5500`.

---

# 4. Используемый стек

Backend:

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
- IntelliJ HTTP Client `.http`
- `curl + jq` для ручной проверки

Frontend:

- HTML
- CSS
- JavaScript
- Bootstrap / jQuery из выбранного шаблона
- dev-запуск через `python3 -m http.server 5500`
- позже: Docker + nginx

---

# 5. Роль ChatGPT в работе над проектом

ChatGPT должен быть:

- наставником;
- техлидом;
- reviewer;
- помощником по архитектуре;
- объясняющим партнёром, а не просто генератором кода.

Работать нужно:

- по шагам;
- без хаотичных скачков;
- с объяснением простыми словами;
- с объяснением места каждого класса в business-flow;
- с учётом уже существующего кода;
- не заставлять пользователя вручную искать по проекту, если архив проекта уже передан;
- не присылать большие куски кода без объяснения, зачем они нужны.

---

# 6. Важный формат объяснения нового кода

Пользователь просил объяснять новый код как **business-flow**, а не просто кидать классы.

При объяснении нового backend-кода желательно отвечать на 10 вопросов:

1. Зачем этот кусок нужен в business-flow.
2. Кто его вызывает.
3. Что приходит на вход.
4. Какие таблицы читаются.
5. Какие таблицы меняются.
6. Какие события создаются.
7. Где начинается и заканчивается транзакция.
8. Что происходит при успехе.
9. Что происходит при ошибке.
10. Как этот кусок связан с остальной Saga.

Пользователю особенно помогают схемы вида:

```text
endpoint
  ↓
controller
  ↓
service
  ↓
repository / DB
  ↓
outbox
  ↓
Kafka
  ↓
consumer
```

Для frontend-кода объяснять нужно так же просто:

```text
страница открылась
  ↓
JS сработал на DOMContentLoaded
  ↓
fetch ушёл в api-gateway
  ↓
backend вернул JSON
  ↓
JS нарисовал карточки товаров
```

---

# 7. Identity/auth архитектура

Принятая модель:

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

- Keycloak отвечает за identity, authentication, credentials lifecycle;
- backend не строит полноценный собственный auth-центр;
- email verification / password reset / password lifecycle — зона Keycloak;
- backend-сервисы работают как OAuth2 Resource Server и сами валидируют JWT;
- API Gateway — входная точка и маршрутизация, но не единственная security-граница;
- downstream-сервисы не должны слепо доверять заголовкам от gateway без собственной security-конфигурации.

Локальная модель:

- `Account` — локальная бизнес-сущность пользователя;
- `Company` — optional business-сущность на будущее.

---

# 8. Роль `auth-module`

`auth-module` остаётся тонким **identity/account provisioning слоем**.

Основной endpoint:

```http
GET /api/v1/auth/me
```

Что делает:

- принимает валидный JWT от Keycloak;
- берёт из JWT `sub`, `email`, `given_name`, `family_name`;
- ищет локальный `Account` по `keycloakUserId`;
- если account отсутствует — создаёт shell/local account;
- возвращает identity/account context.

Важно:

- отдельный публичный `/me/sync` не развиваем как основной API;
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
/auth/me  → identity/account context + provisioning
/users/me → профиль и пользовательские business data
```

---

# 10. Роль `product-service`

`product-service` отвечает за:

- Product / Brand / Category;
- price;
- stockQuantity;
- stock reservation;
- stock release;
- `stock_reservations`;
- consumer-side processing событий от `user-service`;
- product-side outbox events.

Ownership:

```text
product-service владеет:
  Product / Brand / Category / price / stockQuantity / stock_reservations

user-service владеет:
  Account-scoped cart / orders / order status / order history
```

Сейчас dev-БД одна, но логические границы сервисов уже соблюдаются.

---

# 11. Миграции и текущая стратегия БД

Сейчас используется одна общая dev-БД:

```text
app_db
```

Временный подход:

```text
единственный владелец Flyway migrations — auth-module
```

Почему:

- сервисы пока смотрят в одну БД;
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

# 12. Уже собранные базовые backend-flow

## 12.1. Profile flow

```http
GET   /api/v1/users/me
PATCH /api/v1/users/me
```

Логика:

- из JWT берётся `keycloakUserId`;
- по нему находится `Account`;
- отдаётся `CurrentUserResponse`;
- можно обновить профильные поля.

## 12.2. Address flow

```http
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PATCH  /api/v1/users/me/addresses/{addressPublicId}
DELETE /api/v1/users/me/addresses/{addressPublicId}
```

Важно:

- ownership проверяется через `addressPublicId + accountId`;
- реализована default-address логика.

## 12.3. Cart flow

Собрана корзина:

- `Cart`
- `CartItem`
- `CartService`
- `CartController`
- DTO request/response

Важные моменты:

- `CartRepository` использует `@EntityGraph(attributePaths = "items")`;
- `Cart.items` должен иметь `orphanRemoval = true`;
- первый GET корзины не должен быть `readOnly=true`, если при первом запросе корзина создаётся;
- quantity в request DTO валидируется через `@NotNull` / `@Min`.

---

# 13. Product catalog / generator / tooling

В `product-service`:

- публичные GET endpoints каталога;
- write/admin endpoints защищены ролью `ADMIN`;
- есть dev-generator товаров:

```http
POST /api/v1/products/gen/{count}
```

Генератор создаёт товары с:

- `price > 0`;
- `stockQuantity > 0`;
- валидным `publicId`;
- брендом;
- категорией.

Это использовалось для ручных Saga-тестов.

---

# 14. Order Saga-flow: создание заказа

Раньше заказ развивался через более синхронную схему:

```text
user-service
  → ProductCatalogClient
  → product-service REST decrease stock
```

После перехода:

```text
Saga + Outbox + Kafka
```

Теперь:

- `user-service` создаёт заказ и outbox event;
- `product-service` принимает событие и решает, хватает stock или нет;
- результат возвращается в `user-service` через Kafka;
- `user-service` меняет статус заказа по результату.

## 14.1. Пользователь создаёт заказ

```http
POST /api/v1/users/me/orders
```

Business-flow:

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

## 14.2. Product-side reservation

`product-service` получает:

```text
market.order.stock-reservation-requested.v1
```

И в одной транзакции:

```text
1. Проверяет processed_kafka_events
2. Агрегирует items по productPublicId
3. Лочит products FOR UPDATE
4. Проверяет stock
5. Если stock хватает:
   - products.stock_quantity уменьшается
   - stock_reservations получает RESERVED
   - product_outbox_events получает StockReservationSucceeded
6. Если stock не хватает:
   - products.stock_quantity не меняется
   - stock_reservations не создаётся
   - product_outbox_events получает StockReservationFailed
7. INSERT processed_kafka_events
8. COMMIT
```

## 14.3. User-side reservation result

`user-service` получает:

```text
market.product.stock-reservation-result.v1
```

При success:

```text
PENDING_STOCK_RESERVATION → CONFIRMED
```

При failure:

```text
PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED
```

Также сохраняется `statusReason`, если reservation failed.

---

# 15. Cancellation Saga

Cancellation Saga реализована и happy-path вручную проверен.

## 15.1. Пользователь запрашивает отмену

Для заказа в статусе `CONFIRMED` пользователь вызывает cancel endpoint.

Business-flow:

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
    | 4. orders:
    |    CONFIRMED → CANCELLATION_REQUESTED
    | 5. orders.status_reason:
    |    "Пользователь запросил отмену заказа"
    | 6. INSERT order_status_history
    | 7. INSERT order_outbox_events:
    |    OrderCancellationRequested, status=NEW
    | COMMIT
```

## 15.2. Product-service освобождает stock

`product-service` получает:

```text
market.order.cancellation-requested.v1
```

И делает:

```text
1. Проверяет processed_kafka_events
2. Ищет stock_reservations по order_public_id и status=RESERVED
3. Лочит products FOR UPDATE
4. products.stock_quantity += reservation.quantity
5. stock_reservations:
   RESERVED → RELEASED
6. product_outbox_events:
   StockReleaseSucceeded / StockReleaseFailed
7. INSERT processed_kafka_events
8. COMMIT
```

## 15.3. User-service получает release result

`user-service` получает:

```text
market.product.stock-release-result.v1
```

При success:

```text
CANCELLATION_REQUESTED → CANCELLED
statusReason = "Заказ успешно отменён"
```

При failure:

```text
CANCELLATION_REQUESTED → CANCELLATION_FAILED
statusReason = reason из события
```

---

# 16. Что уже проверено руками end-to-end

## 16.1. Success stock reservation

Проверено:

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

## 16.2. Failed stock reservation

Проверено:

```text
order:
  PENDING_STOCK_RESERVATION → STOCK_RESERVATION_FAILED

product stock:
  не уменьшился повторно при failed reservation
```

## 16.3. Happy-path Cancellation Saga

Проверено через API:

```text
CONFIRMED
→ CANCELLATION_REQUESTED
→ CANCELLED
```

Также проверялся товар после отмены:

```text
GET /api/v1/products/public/{productPublicId}
```

Фактически был получен `stockQuantity = 16`. Логически это соответствует возврату stock после отмены, если `stockBefore` был 16.

---

# 17. Kafka retry / DLT

После старого брифа был добавлен и проверен базовый слой retry / DLT.

Созданы DLT topics:

```text
market.order.stock-reservation-requested.v1.dlt
market.order.cancellation-requested.v1.dlt
market.product.stock-reservation-result.v1.dlt
market.product.stock-release-result.v1.dlt
```

Командой из `scripts/kafka-dlt-checks.sh list-topics` было подтверждено наличие topics:

```text
market.order.cancellation-requested.v1
market.order.cancellation-requested.v1.dlt
market.order.stock-reservation-requested.v1
market.order.stock-reservation-requested.v1.dlt
market.product.stock-release-result.v1
market.product.stock-release-result.v1.dlt
market.product.stock-reservation-result.v1
market.product.stock-reservation-result.v1.dlt
```

Что реализовано концептуально:

- `DefaultErrorHandler`;
- `DeadLetterPublishingRecoverer`;
- retry для retriable errors;
- DLT для poison/non-retriable messages;
- `NonRetryableKafkaEventException`;
- consumers принимают `eventId` как optional header:

```java
@Header(name = "eventId", required = false)
```

чтобы missing/blank/invalid `eventId` можно было обработать явно и отправить в DLT.

Важно для нового чата:

- DLT topics уже есть;
- полную ручную проверку poison-message / retry-to-DLT можно выполнить позже;
- базовый list-topics smoke test уже проходил.

---

# 18. Order lifecycle cleanup

После проверки Saga начали выравнивать жизненный цикл заказа под новую Saga-модель.

## 18.1. `AdminOrderServiceImpl`

Класс:

```text
user-service/src/main/java/ru/example/userservice/service/impl/AdminOrderServiceImpl.java
```

Старый admin-flow был ориентирован на старые статусы:

```text
CREATED → PROCESSING
PAID → PROCESSING
PROCESSING → CANCELLED
```

Это конфликтовало с Saga, потому что новый заказ теперь сначала проходит reservation:

```text
PENDING_STOCK_RESERVATION → CONFIRMED
```

Принята новая admin transition model:

```text
CONFIRMED → PROCESSING → SHIPPED → DELIVERED
```

Карта переходов оставлена такой:

```java
private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING),
        OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
        OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED, Set.of(),
        OrderStatus.CANCELLED, Set.of(),
        OrderStatus.STOCK_RESERVATION_FAILED, Set.of(),
        OrderStatus.CANCELLATION_REQUESTED, Set.of(),
        OrderStatus.CANCELLATION_FAILED, Set.of(),
        OrderStatus.PENDING_STOCK_RESERVATION, Set.of()
);
```

Смысл:

```text
админ может двигать только подтверждённый заказ:
CONFIRMED → PROCESSING → SHIPPED → DELIVERED

админ не может трогать Saga/system statuses:
PENDING_STOCK_RESERVATION
STOCK_RESERVATION_FAILED
CANCELLATION_REQUESTED
CANCELLATION_FAILED
CANCELLED
```

Также `AdminOrderServiceImpl` был переведён на общий механизм смены статуса через:

```text
OrderStatusTransitionService
```

Вместо прямого:

```java
order.setStatus(targetStatus);
```

нужно использовать:

```java
orderStatusTransitionService.changeStatus(
        order,
        targetStatus,
        "Статус заказа изменён администратором"
);
```

После правки `user-service` стартовал успешно.

## 18.2. `Order.java` fallback status

Файл:

```text
identity-domain/src/main/java/ru/example/identitydomain/entity/Order.java
```

В `@PrePersist` был старый fallback:

```java
if (status == null) {
    status = OrderStatus.CREATED;
}
```

Он заменён на:

```java
if (status == null) {
    status = OrderStatus.PENDING_STOCK_RESERVATION;
}
```

Смысл:

```text
если кто-то в будущем создаст Order без явного статуса,
заказ не попадёт в старую модель CREATED,
а попадёт в новую Saga-модель PENDING_STOCK_RESERVATION.
```

---

# 19. Старые статусы `CREATED` / `PAID`

В `OrderStatus` могут ещё оставаться старые значения:

```text
CREATED
PAID
```

Их не нужно резко удалять первым шагом, потому что:

- в БД могут быть старые записи;
- удаление enum-value может сломать чтение старых заказов через JPA.

Текущий безопасный подход:

```text
CREATED / PAID пока могут оставаться в enum,
но admin-flow больше не должен разрешать переходы из них.
```

Позже можно отдельно решить:

- мигрировать старые заказы;
- пометить статусы deprecated;
- удалить после миграции.

---

# 20. Frontend: выбранный шаблон

После сравнения нескольких архивов выбран шаблон:

```text
e-commerce-template-main
```

Источник/репозиторий, который обсуждался:

```text
bilalDevX/e-commerce-template
```

Почему выбран он:

- это не PHP/MySQL backend;
- это обычный frontend: HTML + CSS + JavaScript + Bootstrap/jQuery;
- он содержит больше нужных страниц, чем MultiShop.

Страницы, которые есть:

```text
index.html              — главная
store.html              — каталог / магазин
product-detail.html     — карточка товара
cart.html               — корзина
place-order.html        — оформление заказа
order_complete.html     — успешный заказ
dashboard.html          — личный кабинет / dashboard
search-result.html      — результаты поиска
signin.html             — вход
register.html           — регистрация
```

Сравнение:

```text
MultiShop:
  есть главная, каталог, корзина, checkout, contact
  нет product detail
  нет login/register
  нет dashboard

bilalDevX/e-commerce-template:
  есть главная
  есть каталог
  есть product detail
  есть cart
  есть checkout/place order
  есть order complete
  есть dashboard
  есть signin/register
  есть search result
```

Итог:

```text
из проверенных бесплатных шаблонов это лучший вариант,
если цель — минимум писать HTML руками.
```

---

# 21. Текущая frontend-структура в проекте

Пользователь уже применил структуру:

```text
Market/
  api-gateway/
  auth-module/
  user-service/
  product-service/
  identity-domain/
  frontend/
    index.html
    store.html
    product-detail.html
    cart.html
    place-order.html
    signin.html
    register.html
    dashboard.html
    css/
    js/
    images/
    Dockerfile
    nginx.conf
```

Frontend сейчас рассматривается как отдельный web-сервис.

Важно:

- frontend пока не Gradle module;
- backend запускается отдельно;
- frontend должен обращаться только через `api-gateway`, а не напрямую в `user-service` или `product-service`.

Правильная схема:

```text
Browser
  ↓
frontend
  ↓ HTTP/fetch
api-gateway
  ↓
auth-module / user-service / product-service
```

---

# 22. Как сейчас запускать frontend

Для dev-режима пока не нужен Docker.

Пока backend запускается как обычно из IntelliJ + Docker infra, frontend лучше запускать простым static server:

```bash
cd frontend
python3 -m http.server 5500
```

Пользователь захотел команду через Makefile.

В корне проекта `Market/Makefile` нужно иметь target:

```makefile
.PHONY: front

front:
	cd frontend && python3 -m http.server 5500
```

Запуск из корня проекта:

```bash
make front
```

Открывать:

```text
http://localhost:5500/index.html
```

Остановка:

```text
Ctrl + C
```

Важно: строка с `cd frontend...` в Makefile должна начинаться с **Tab**, не с пробелов.

---

# 23. Нужно ли сейчас запускать frontend в Docker

Пока **нет**.

Причина:

- сейчас фронт активно будет меняться;
- через `python3 -m http.server` быстрее проверять HTML/JS/CSS;
- не нужно пересобирать Docker image после каждой правки;
- проще смотреть ошибки в браузере.

Docker/nginx понадобится позже, когда frontend будет подключён к backend и потребуется собрать всё через Docker Compose.

Будущая production-like схема:

```text
frontend container
  ↓ nginx
  ↓ раздаёт HTML/CSS/JS
  ↓ proxy /api/** в api-gateway
api-gateway
  ↓
services
```

---

# 24. Как frontend будет подключаться к backend

На первом dev-этапе проще:

```text
frontend:
  http://localhost:5500

api-gateway:
  http://localhost:8080
```

JS может временно ходить так:

```js
const API_BASE_URL = "http://localhost:8080";
```

и выполнять:

```js
fetch(`${API_BASE_URL}/api/v1/products/public`)
```

Возможна CORS-ошибка, потому что:

```text
localhost:5500 → localhost:8080
```

Если CORS появится, нужно будет править CORS в `api-gateway`.

Позже, когда frontend будет в nginx/Docker, лучше перейти на относительные пути:

```js
fetch("/api/v1/products/public")
```

и проксировать `/api/**` из nginx в `api-gateway`.

---

# 25. С чего начать соединение frontend + backend

Не подключать всё сразу.

Порядок:

```text
1. store.html
   подключить каталог товаров

2. product-detail.html
   подключить карточку одного товара

3. signin.html / register.html
   подключить auth flow / token handling

4. cart.html
   подключить корзину

5. place-order.html
   подключить checkout и создание заказа

6. dashboard.html
   подключить профиль и список заказов
```

Первый шаг нового чата:

```text
Подключить store.html к реальным товарам через api-gateway.
```

Ожидаемый flow:

```text
Пользователь открывает store.html
  ↓
JS на странице срабатывает на DOMContentLoaded
  ↓
fetch идёт в api-gateway
  ↓
api-gateway маршрутизирует в product-service
  ↓
product-service возвращает JSON товаров
  ↓
JS очищает демо-товары шаблона
  ↓
JS рисует реальные карточки товаров
```

---

# 26. Что нужно сделать в новом чате первым делом

В новом чате нужно начать именно с frontend integration, а не с нового backend-слоя.

Порядок первого этапа:

```text
1. Пользователь загружает актуальный архив проекта.
2. Пользователь загружает этот бриф.
3. Ассистент смотрит структуру frontend.
4. Ассистент находит в store.html блок, где лежат demo-products.
5. Ассистент создаёт/предлагает простой JS-слой:
   frontend/js/api.js
   frontend/js/store-page.js
6. Подключает store-page.js к store.html.
7. Первый API-запрос:
   GET товаров через api-gateway.
8. Проверка в браузере:
   http://localhost:5500/store.html
9. Если CORS — правим api-gateway.
```

---

# 27. Что НЕ нужно делать первым

Не начинать сразу с:

```text
cart
checkout
login
Docker frontend
nginx proxy
полная авторизация
React/TypeScript
переписывание всего шаблона
```

Сначала только:

```text
store.html → реальные товары из backend
```

Это даст быстрый видимый результат.

---

# 28. Состояние проекта на момент перехода в новый чат

Текущее состояние:

- Order Saga creation через Outbox + Kafka реализована и вручную проверена;
- success reservation проверен;
- failed reservation проверен;
- Cancellation Saga happy-path проверен через API;
- DLT topics созданы и list-topics проверен;
- retry/DLT базово добавлен, полный poison-message тест можно сделать позже;
- admin order transitions выровнены под Saga-модель;
- `Order.java` fallback изменён с `CREATED` на `PENDING_STOCK_RESERVATION`;
- выбран frontend-шаблон `bilalDevX/e-commerce-template`;
- шаблон помещён в папку `frontend`;
- frontend запускается dev-командой `make front`;
- следующий этап — соединить frontend с backend через API Gateway.

---

# 29. Как начать новый чат

В новом чате пользователь должен написать примерно так:

```text
Я продолжаю проект Market.
Вот актуальный архив проекта и обновлённый бриф.
Мы закончили Saga/Outbox/Kafka этап, выбрали frontend-шаблон и теперь хотим начать соединять frontend с backend через api-gateway.
Первый шаг — подключить store.html к реальным товарам из product-service.
Объясняй всё простыми словами и как business-flow.
```

Первый ответ ассистента в новом чате должен быть:

```text
1. Принять архив и бриф.
2. Посмотреть структуру frontend.
3. Найти demo-products в store.html.
4. Предложить минимальный план подключения store.html к product-service через api-gateway.
5. Начать с маленькой правки: api.js + store-page.js.
```

---

# 30. Главная цель следующего этапа

Сделать так, чтобы проект перестал быть только backend API и стал полноценным приложением:

```text
Frontend template
  ↓
реальные товары
  ↓
карточка товара
  ↓
корзина
  ↓
checkout
  ↓
создание заказа
  ↓
Saga status tracking
```

Первый конкретный milestone:

```text
store.html показывает реальные товары из product-service через api-gateway.
```
