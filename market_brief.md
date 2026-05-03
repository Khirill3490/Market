# Market Project — актуальный рабочий бриф для новых чатов

## 1. Что это за проект

Это мой Java / Spring Boot multi-module проект, который я хочу довести до уровня **уверенного production-style backend проекта**, а не просто учебного pet-проекта. Базовая структура проекта: `api-gateway`, `auth-module`, `user-service`, `product-service`, `eureka-server`, `common`, `identity-domain`, `frontend` и служебные модули. fileciteturn50file0

### Используемый стек
- Java
- Spring Boot
- Spring Cloud
- Eureka
- Spring Cloud Gateway
- Spring Security
- Keycloak
- PostgreSQL
- Redis
- JPA / Hibernate
- Flyway
- Gradle
- Docker Compose
- HTML / JS frontend fileciteturn50file0

---

## 2. Главная цель

Нужно не просто “дописать фичи”, а **постепенно превратить проект в полноценный production-style backend интернет-магазина**. Под этим подразумевается:
- стабильная сборка из корня проекта
- единый и чистый Gradle multi-module setup
- отсутствие compile errors и недоделанных кусков
- безопасная работа с конфигами и секретами
- миграции БД через Flyway
- понятная архитектура сервисов
- хорошие практики безопасности
- тесты
- observability
- CI/CD база
- понятный roadmap развития fileciteturn50file0

---

## 3. Как мне нужно помогать в новых чатах

### Роль ChatGPT
Ты для меня не просто отвечающий помощник, а **наставник / техлид / reviewer**, который:
- ведет меня по проекту по шагам
- помогает принимать архитектурные решения
- объясняет, зачем нужна каждая технология
- объясняет не только “что писать”, но и “почему так”
- подсказывает, что в проекте хорошо, а что плохо
- следит, чтобы проект двигался именно к production-подходу fileciteturn50file0

### Формат помощи
Мне нужен стиль:
- пошагово
- без лишней воды
- как teacher / mentor style
- сначала маленькие, понятные шаги, потом следующий этап
- **обязательно разбирать код**: что делает каждый фрагмент, как он работает в цепочке, зачем он нужен и почему он устроен именно так

### Важное правило
**Не просто присылать код, а обязательно разбирать его.** Я хочу понимать, как всё устроено, а не слепо копировать. fileciteturn50file0

---

## 4. Архитектурная основа, которая уже принята

### Главный поворот
Мы **не строим локальный auth-центр**. Принято решение, что:
- **Keycloak** отвечает за identity / authentication / credential lifecycle
- локальный backend **не должен** развивать старый `signin / refresh / logout` как основную auth-модель
- email verification, password reset и credential lifecycle — зона ответственности Keycloak fileciteturn50file0

### Целевая модель
Проект движется к модели:

**Keycloak identity -> local Account -> optional Company** fileciteturn50file0

Это означает:
- `Keycloak` — identity provider
- `auth-module` — тонкий identity/account-provisioning слой
- `Account` — локальная доменная сущность пользователя магазина
- `Company` — отдельная **опциональная** бизнес-сущность
- `user-service` — слой профиля и пользовательских бизнес-данных fileciteturn50file0turn49file14

### Важное правило
`Company` **не является обязательной** частью любого `Account`: обычный customer живёт без `Company`, seller/business flow должен быть отдельным сценарием. В самой сущности `Account` `Company` сейчас действительно optional. fileciteturn50file0turn49file14

---

## 5. Security и Keycloak — принятый подход

### Общий принцип
Принято решение использовать **Keycloak как единственный identity/auth слой**, а backend-сервисы делать **OAuth2 Resource Server**. fileciteturn50file0

### Практический смысл
- пользователь логинится не в локальный backend, а через Keycloak
- frontend / gateway инициирует внешний auth flow
- клиент получает access token
- gateway — внешняя точка входа и может прокидывать token дальше
- **каждый backend-сервис, который принимает пользовательские HTTP-запросы, сам валидирует JWT**
- мы **не строим** схему, где только один сервис валидирует токен, а остальные слепо доверяют заголовкам fileciteturn50file0

### Важный вывод
`api-gateway` — это входная точка, маршрутизация и, при необходимости, token relay, но **не единственный охранник**, которому все остальные сервисы безусловно доверяют. Каждый HTTP-сервис должен жить как самостоятельный resource server. fileciteturn50file0

---

## 6. Что уже реально сделано по Keycloak и инфраструктуре

### Локальная связка уже поднималась и проверялась
Уже поднимались и совместно работали:
- `postgres`
- `keycloak`
- `eureka-server`
- `auth-module`
- `user-service`
- `api-gateway`

Ключевая рабочая цепочка уже проверялась: **Keycloak -> gateway -> auth-module -> user-service**. По ручному прогону `/api/v1/auth/me` через gateway успешно возвращался account context по валидному JWT. fileciteturn49file9turn49file3

### Что настроено в Keycloak локально
Локально уже настраивались:
- realm `market`
- browser client `market-web`
- технический client `market-cli` для ручного получения access token
- тестовый пользователь для smoke-тестов

Это пока локальная dev-конфигурация, но она уже позволила вручную прогонять JWT flow. Осмысленно нужно держать в голове, что дальше хорошо бы прийти к realm export/import. Это уже было зафиксировано в старом брифе как правильная цель. fileciteturn50file0

---

## 7. Текущая роль `auth-module`

### Что он делает сейчас
`auth-module` оставлен как **identity/account-provisioning слой**. Его публичный endpoint — `GET /api/v1/auth/me`. Внутри он теперь не просто “смотрит контекст”, а умеет по валидному JWT **найти или создать локальный `Account`**. Это реализовано в `CurrentAccountService`: он берёт `sub`, `email`, `given_name`, `family_name`, ищет `Account` по `keycloakUserId`, а если записи нет — создаёт локальный account через `AccountServiceImpl`. fileciteturn49file9turn49file8

### Что это означает
- отдельный публичный `/me/sync` **не нужен**
- provisioning локального `Account` стал внутренней серверной механикой
- первый вызов `/auth/me` уже может замкнуть Keycloak identity на локальную доменную модель fileciteturn50file0turn49file9

### За что `auth-module` отвечает
- current identity context
- связка `Keycloak user -> local Account`
- внутренний provisioning локального `Account`
- технические account / identity use-case’ы fileciteturn50file0turn49file9

### Что не считаем целевым публичным API
По-прежнему не считаем целевыми:
- `signin`
- `refresh`
- `logout`
- `activate`
- `forgotPassword`
- `resetPassword`
- публичный `/me/sync` fileciteturn50file0

---

## 8. Текущая роль `user-service`

### Что он должен делать
`user-service` **не должен дублировать** `auth-module`. Его зона ответственности:
- профиль пользователя
- пользовательские данные приложения
- customer/business context
- адреса
- корзина
- дальше — настройки, кабинет, seller/customer flows fileciteturn50file0turn49file3turn49file1turn49file0

### Разграничение `/auth/me` и `/users/me`
`auth-module /auth/me` — это endpoint про:
- identity context
- факт существования локального `Account`
- базовую связку Keycloak identity с local Account
- внутренний provisioning `Account` при первом заходе fileciteturn50file0turn49file9

`user-service /users/me` — это endpoint про:
- профиль пользователя в приложении
- чтение и обновление профильных полей локального `Account`
- не identity-flow, а уже user/business data flow fileciteturn50file0turn49file2turn49file3

### Критическое правило
Нельзя допускать, чтобы `auth-module` и `user-service` превратились в два одинаковых сервиса с одинаковым `/me`. fileciteturn50file0

---

## 9. Что уже реально сделано в `user-service`

### Профиль текущего пользователя
Уже собран профильный flow:
- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`

`CurrentUserService` сейчас:
- извлекает `keycloakUserId` из `Jwt`
- ищет локальный `Account`
- для `GET` возвращает `CurrentUserResponse`
- для `PATCH` обновляет только профильные поля (`firstName`, `lastName`, `phone`) и возвращает обновлённый DTO fileciteturn49file2turn49file3

### Address flow
Уже собран адресный слой:
- список адресов текущего пользователя
- создание адреса
- обновление адреса
- удаление адреса

`AddressService` работает от текущего `Account`, а не от переданного userId, и все операции делает только внутри адресов текущего пользователя. Поиск адреса идёт по связке `addressPublicId + accountId`, а не просто по `publicId`, что защищает от доступа к чужим адресам. В сервисе уже реализована логика default-адреса: если новый адрес создаётся как основной, прежний default-адрес сбрасывается; при update можно сделать адрес новым default. Ручной smoke-test списка адресов через gateway уже успешно выполнялся. fileciteturn49file1turn49file6turn49file7

### Cart flow — уже начат
Уже собран каркас корзины:
- `CartService`
- сущности корзины и позиций корзины
- DTO и controller под текущего пользователя

Текущая логика `CartService`:
- по JWT находит текущий `Account`
- находит или создаёт корзину текущего пользователя
- умеет добавлять товар в корзину, увеличивая `quantity`, если товар уже есть
- умеет менять количество позиции
- умеет удалять позицию
- умеет очищать корзину целиком
- возвращает `CartResponse` с items и `totalItems` fileciteturn49file0turn49file5

Важно: cart flow уже реализован по структуре, но в новом чате стоит проверить его целиком руками так же, как проверялись адреса. Это ещё не тот кусок, который стоит считать полностью отполированным до прод-уровня. Выше всего обратить внимание на транзакции: `getCurrentUserCart(...)` сейчас помечен `readOnly = true`, хотя внутри может создавать корзину, и это нужно дочистить. fileciteturn49file0

---

## 10. Состояние доменной модели

### `Account`
`Account` уже оформлен как хорошая локальная сущность:
- `publicId`
- `keycloakUserId`
- `email`
- `firstName`
- `lastName`
- `phone`
- `accountType`
- `status`
- optional `Company`
- `createdAt`
- `updatedAt`

На уровне таблицы есть уникальные ограничения на `public_id`, `keycloak_user_id` и `email`. Это соответствует логике provisioning и текущему use-case’у профиля. fileciteturn49file14

### `Address`
`Address` уже добавлен как отдельная пользовательская бизнес-сущность, принадлежащая `Account`. Это первый настоящий user-domain объект поверх профиля. Он уже поддерживает default-адрес и хранит адресную информацию отдельно от базового профиля. fileciteturn49file1

### `Cart` и `CartItem`
Добавлена модель корзины текущего пользователя. На текущем этапе корзина хранит позиции по `productPublicId`, а не через внешний ключ к таблице товаров, что хорошо для разнесённой модульной архитектуры и будущего разделения bounded context’ов. fileciteturn49file0

---

## 11. Что это означает технически

Нас всё ещё ждёт существенный рефакторинг, но фундамент уже заметно продвинулся. Нужно продолжать доводить до зрелого вида:
- сущности
- репозитории
- сервисы
- DTO
- контроллеры
- security-конфигурацию по модулям
- миграции Flyway
- разграничение ответственности между сервисами
- error handling
- тесты fileciteturn50file0

Но важное изменение по сравнению со старой картиной в том, что теперь проект уже не просто “готовится к новой архитектуре”, а **частично в ней работает**:
- provisioning локального `Account` уже есть
- профиль уже работает вокруг `Account`
- адреса уже работают вокруг `Account`
- корзина уже начата на той же модели fileciteturn49file9turn49file3turn49file1turn49file0

---

## 12. Что уже практически сделано

### По `auth-module`
Уже сделано:
- модуль упрощён под новую модель
- убран legacy local auth flow
- оставлен честный `GET /api/v1/auth/me`
- модуль работает как resource server
- provisioning локального `Account` встроен внутрь flow `/auth/me` через `CurrentAccountService` и `AccountServiceImpl` fileciteturn50file0turn49file9turn49file8

### По `user-service`
Уже сделано:
- собран профиль текущего пользователя (`GET /users/me`, `PATCH /users/me`)
- собран address flow
- собран каркас cart flow
- сервис строится вокруг `Account`, а не вокруг старого `User` fileciteturn49file2turn49file3turn49file1turn49file0

### По архитектурному пониманию
Уже зафиксировано и частично проверено руками:
- Keycloak логинит пользователя
- gateway может прокидывать access token дальше
- backend-сервисы сами валидируют JWT
- `auth-module` и `user-service` реально разведены по ответственности
- базовая цепочка identity → local account → profile → addresses → cart уже существует в коде fileciteturn50file0turn49file9turn49file3turn49file1turn49file0

---

## 13. Обновлённый roadmap

### Этап 1. Техническая стабилизация проекта
Цель: сделать проект стабильно собираемым и запускаемым.

Что нужно делать:
- держать модули в едином состоянии
- убирать compile errors и рассинхроны имен/контрактов
- привести конфиги и секреты в порядок
- держать working local flow через Keycloak + gateway + eureka + auth-module + user-service fileciteturn50file0

### Этап 2. Доведение identity/account слоя
Что нужно делать:
- удержать `auth-module` как identity/provisioning слой
- не плодить публичный `/me/sync`
- дочистить транзакции и edge-cases provisioning’а
- зафиксировать текущую модель `Account + optional Company` как локальную основу проекта fileciteturn50file0turn49file9turn49file14

### Этап 3. Доведение `user-service`
Что нужно делать:
- подчистить `CurrentUserService`, `AddressService`, `CartService`
- добавить/удержать единый `ErrorResponse` и `@RestControllerAdvice`
- руками прогнать профиль, адреса и корзину end-to-end
- решить edge-cases по default address и cart semantics fileciteturn49file3turn49file1turn49file0

### Этап 4. Бизнес-слой магазина
Что нужно делать:
- завершить корзину
- затем переходить к заказам
- после этого — seller/business onboarding, company lifecycle, product ownership / management

Последовательность, которая сейчас выглядит естественной:
**profile -> addresses -> cart -> orders -> seller/company flow**. fileciteturn50file0turn49file1turn49file0

### Этап 5. Общая зрелость платформы
Что нужно делать:
- единый security-подход по всем HTTP-сервисам
- выравнивание `product-service`
- тесты
- observability
- CI/CD
- позже — shared security module, если повторяющийся security-код действительно дозреет до вынесения fileciteturn50file0

---

## 14. Что пока не нужно делать слишком рано

Пока не надо без необходимости навешивать:
- Kafka
- Saga
- Kubernetes
- Vault
- event-driven всё подряд
- service mesh
- чрезмерную distributed complexity

Сначала нужен **крепкий фундамент** вокруг identity/account/profile/address/cart и только потом более тяжёлая distributed-инфраструктура. fileciteturn50file0turn49file1turn49file0

---

## 15. Приоритетный порядок работ сейчас

Вот текущий правильный порядок:

1. **Удержать и дочистить `auth-module` и `user-service` в новой модели**
2. **Подтвердить руками end-to-end flow через Keycloak + gateway**
3. **Дочистить профиль, адреса и корзину**
4. **Затем идти в заказы**
5. **После этого переходить к seller/company flow и product ownership**
6. **Параллельно выравнивать `product-service` по security и зрелости**
7. **Потом усиливать тесты, observability и CI/CD** fileciteturn49file9turn49file3turn49file1turn49file0turn50file0

---

## 16. Как нужно объяснять технологии и код

Это критически важный пункт для новых чатов.

Мне нужно, чтобы помощь была не в формате “держи код”, а в формате:
- сначала коротко определить этап
- потом объяснить, зачем изменение нужно
- потом показать код
- потом **обязательно разобрать код**:
  - что делает каждый фрагмент
  - как он работает в цепочке запроса / бизнес-логики
  - почему он нужен
  - почему выбран именно такой вариант
- потом коротко сказать, как это проверить руками

### Важное правило
**Не просто присылать код, а обязательно разбирать его.**

И ещё одно правило:
- идти маленькими шагами
- не перепрыгивать сразу через полпроекта
- не делать большие необъяснённые пачки изменений fileciteturn50file0

---

## 17. Как продолжать работу в новом чате

Когда я прихожу в новый чат, нужно опираться на этот документ и продолжать с позиции, что:
- у меня multi-module проект Market
- Keycloak выбран как единственный identity/auth слой
- backend-сервисы работают как resource servers
- gateway — входная точка, но не единственный проверяющий слой
- локальная доменная модель уже переведена на `Account + optional Company`
- `auth-module` уже умеет provisioning локального `Account`
- `user-service` уже умеет профиль текущего пользователя
- `user-service` уже умеет адреса текущего пользователя
- `user-service` уже имеет каркас корзины
- следующий естественный use-case после доведения корзины — заказы fileciteturn50file0turn49file9turn49file3turn49file1turn49file0

### Предпочтительный формат работы
Лучший формат работы со мной:
1. коротко определить текущий этап
2. сказать, что именно делаем сейчас
3. идти маленькими шагами
4. обязательно объяснять код и архитектурную логику
5. фиксировать, что уже улучшили
6. только потом переходить к следующему шагу fileciteturn50file0

---

## 18. Ближайшая практическая цель

### Фаза 1 — стабилизация identity/account/user слоя
- окончательно удержать `auth-module` как identity / provisioning слой
- удержать `user-service` как профильный и пользовательский бизнес-сервис
- дочистить транзакции и edge-cases в профильном, address и cart сервисах fileciteturn49file9turn49file3turn49file1turn49file0

### Фаза 2 — user-domain магазинного слоя
- профиль
- адреса
- корзина
- затем заказы fileciteturn49file3turn49file1turn49file0

### Фаза 3 — business growth
- company / seller flow
- product ownership / management
- order lifecycle
- дальше — observability, tests, CI/CD и общая зрелость платформы fileciteturn50file0

---

## 19. Короткая формулировка текущей цели

**Я хочу превратить текущий Market-проект в production-style backend интернет-магазина, где Keycloak отвечает за identity/auth, gateway является входной точкой, backend-сервисы работают как resource servers, `auth-module` отвечает за identity/account provisioning, а локальный backend хранит и обслуживает свои доменные бизнес-сущности вокруг модели `Account + optional Company`. На текущем этапе уже построен слой identity -> local account -> profile -> addresses -> cart, и дальше проект нужно развивать в сторону orders, seller/company flow и общей зрелости платформы. При этом мне важно не просто получать код, а понимать, как и почему он работает.** fileciteturn50file0turn49file9turn49file3turn49file1turn49file0


## Для ответов чата

Формат объяснения кода

Когда ChatGPT предлагает код, класс, сервис, контроллер, DTO, entity, repository, config или миграцию, нужно объяснять не только “что делает код”, но и его место в общей цепочке проекта:

1. где этот класс находится в архитектуре;
2. кто его вызывает;
3. на каком этапе request/business flow он срабатывает;
4. зачем он нужен;
5. что делает внутри по шагам;
6. как связан с другими классами;
7. какие есть нюансы и технические долги;
8. как проверить это руками.

Цель — не просто копировать готовый код, а понимать, как он работает в системе.