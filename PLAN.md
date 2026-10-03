# Project: Family Tree MVP (Spring Boot 3.2.5 + Thymeleaf)

**Goal:** Развернутый веб-сайт с визуализацией семейного древа (муж+жена — их дети). Авторизованный пользователь («Муж») может редактировать только свою карточку (текстовые поля), остальные недоступны для изменения.

---

## ✅ Фаза 1: Инфраструктура и конфигурация

| ID | Задача | Acceptance Criteria |
|----|--------|---------------------|
| ✅ FEAT-001 | Инициализация структуры проекта Spring Boot | Репозиторий создан на GitHub; pom.xml настроен. Локальная сборка `mvn clean package` успешна. |
| ✅ INFRA-002 | Настройка Docker Compose для локальной разработки | docker-compose.yml содержит app и db. Приложение подключается к БД без ошибок. |
| ✅ INFRA-003 | Настройка схемы БД и миграций (Flyway) | Таблица person существует. DDL применяется при старте без SQL-ошибок. Spring Boot 3.2.5 + Flyway 9.22.3. |

---

## ✅ Фаза 2: Модель данных и Core API (Backend Foundation)

| ID | Задача | Dependencies | Acceptance Criteria                                                                                                                                                                                                                                      |
|----|--------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ✅ BE-004-DTO | Создание DTO-классов | INFRA-003 | `PersonRequest` (для создания/редактирования), `PersonResponse` (для отображения), `TreeResponse` (граф семьи). Поля дублируют Person, но без `@ManyToOne` связей. DTO отделены от Entity.                                                               |
| ✅ BE-004-HIERARCHY | Создание JPA Entity Person со связями | BE-004-DTO, INFRA-003 | Поля: id, firstName, lastName, bio, photoUrl, birthDate (LocalDate), deathDate (LocalDate, nullable=true), ownerUsername, isClaimed. Добавлены связи: `@ManyToOne private Person father;`, `@ManyToOne private Person mother;`. Lombok применен.         |
| ✅ BE-005 | Реализовать PersonRepository | BE-004-HIERARCHY | Интерфейс наследуется от JpaRepository. Методы: `findByOwnerUsername()`, `findById()`, `findAll()`.                                                                                                                                                      |
| ✅ BE-006-TREE | Создать слой сервисов (Tree Logic) | BE-005 | Методы CRUD реализованы. Добавлен метод `getFamilyTree()`, возвращающий корень (мужа) с заполненными связями wife и children. В заглушках значение deathDate равно null. Написаны unit-тесты через Mockito. Выделен бин `PermissionService` (`isOwner`). |
| ✅ BE-008-STUB | Загрузка фото (Stub) | BE-006-TREE | Контроллер `POST /api/persons/{id}/photo` принимает строку-ссылку. URL сохраняется в `photoUrl`.                                                                                                                                                         |
| ❌ BE-008-UPLOAD | Загрузка фото в локальное хранилище | BE-008-STUB | (Перенести в MVP 2.0) Сервис-метод загрузки файла на диск (папка `./uploads/`). Генерирует UUID-имя, сохраняет URL в `photoUrl`. Валидация: размер ≤ 5MB, MIME-тип image/jpeg/png.                                                                       |
| ✅ CLAIM-017 | Закладка фундамента MVP-2.0 | BE-006-TREE | Эндпоинт `PATCH /api/persons/{id}/claim` устанавливает владельца профиля.                                                                                                                                                                                |

---

## ✅ Фаза 2.5: DTO, валидация и обработка ошибок

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ BE-017-VALIDATION | Bean Validation на DTO | BE-004-DTO | `@NotBlank` на firstName, lastName. `@Past` на birthDate. `@Size(max=2000)` на bio. Валидация срабатывает на контроллере с `@Valid`. |
| ✅ BE-016-EXCEPTION | Global Exception Handler | BE-017-VALIDATION | `@ControllerAdvice` перехватывает `MethodArgumentNotValidException`, `EntityNotFoundException`, `AccessDeniedException`. Возвращает JSON-ошибки с HTTP-кодом и сообщением. |

---

## ✅ Фаза 2.6: Защита персональных данных (PII Masking)

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ PII-001 | Создать `MaskingUtil` утилитный класс | None | `maskLastName("Иванов")` → `"И*****"`, `maskDate(LocalDate)` → `"1990"` |
| ✅ PII-002 | Создать `PersonPublicResponse` DTO | PII-001 | `birthDate`/`deathDate` → String (год), `lastName` → маскированное |
| ✅ PII-003 | Обновить `TreeResponse` | PII-002 | Использует `PersonPublicResponse` вместо `PersonResponse` |
| ✅ PII-004 | Добавить публичные методы в `PersonService` | PII-003 | `getByIdPublic()`, `findAllPublic()`, `toPublicResponse()` |
| ✅ PII-005 | Обновить `PersonController` | PII-004 | Все эндпоинты возвращают `PersonPublicResponse`, удалён дублирующий `toResponse()` |
| ✅ PII-006 | Добавить тесты на маскировку | PII-005 | `MaskingUtilTest`, `PersonControllerMaskingTest`, обновлён `PersonServiceTest` |

---

## Фаза 3: Безопасность и авторизация (Security Foundation)

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ SEC-011 | In-Memory Authentication (Минимум) | None | Задано два пользователя: `admin` (roles USER, MASTER), `user` (role USER). Форма входа появляется при обращении к защищенным ресурсам. Logout работает. Пользователи вынесены в `application.properties`. |
| ✅ SEC-012 | Ограничение прав (Владение) | SEC-011 | Правило вынесено в `PermissionService.canEdit(id, username, repository, authentication)`: `return isOwner || hasMasterRole(auth)`. Контроллер `/persons/{id}/edit` возвращает HTTP 403, если `canEdit == false`. |
| ✅ SEC-013 | Проверка Мастер-прав (Подготовка) | SEC-012 | Логика метода `canEdit()` обновляется: `(является владельцем) OR (имеет роль ROLE_MASTER)`. |
| ✅ SEC-014 | Dual-response: авторизованные видят полные данные | SEC-013 | `PersonController` (REST) проверяет `Authentication`. Авторизованный пользователь получает `PersonResponse` (полные данные), неавторизованный — `PersonPublicResponse` (маскированные). Tree page (`/tree`) также показывает полные данные для авторизованных пользователей (через модель). |

---

## Фаза 4: Frontend Views (Thymeleaf)

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ FE-008 | Bootstrap 5 и базовый лейаут | None | Мастер-шаблоны `_header.html` (с кнопкой «Войти») и `_footer.html`. Адаптивное меню. |
| ✅ FE-010-VISUAL | Главная страница как дерево | BE-006-TREE, BE-008-STUB | Лендинг `/` отображает схему: блок «Муж + Жена», соединенный линиями с блоками «Сын» и «Дочь». Реализация: CSS-коннекторы (без JS-библиотек). При клике на элемент — переход на `/persons/{id}`. |
| ✅ SEC-016 | Страница `/register` (заглушка) | None | URL `/register` → Thymeleaf-страница «Feature coming soon». |
| ✅ FE-009-CARD-PREFILL | Предзаполненная карточка профиля | FE-010-VISUAL | Страница `/persons/{id}` показывает ФИО, даты, фото. Дата смерти **не рендерится вообще** (ни тега, ни условия `th:if`). Поле «Био» присутствует визуально, но пустое (или имеет плейсхолдер). Для авторизованных — полные данные, для гостей — маскированные. |
| ✅ FE-013-RESTRICTED-FORM | Форма редактирования (без фото) | FE-009-CARD-PREFILL | Страница `/persons/{id}/edit` предзаполнена. Содержит инпуты текста и дат. Инпутов для загрузки фото и ввода даты смерти нет. |
| ✅ FE-015 | Условное отображение кнопки | SEC-012, FE-013 | Кнопка «Редактировать» видна только если `canEdit == true`. |

---

## Фаза 5: Функционал редактирования (MVP Complete)

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ BE-014 | Обработчик сохранения правок | BE-006-TREE, FE-013-RESTRICTED-FORM | Контроллер принимает данные формы. Валидирует ввод. Сохраняет текст/биографию/даты в PostgreSQL. Редиректит на просмотр с сообщением об успехе. Фото не трогается. |

---

## Фаза 5.2: Исправление ошибок (SecurityConfig — публичные маршруты)

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ BUG-001 | Добавить `/tree` в permitAll() | None | `SecurityConfig` содержит `/tree` в списке permitAll. Неавторизованный пользователь переходит по кнопке "Показать дерево" с `/` → `/tree` без редиректа на `/login`. На `/tree` отображается дерево с маскированными данными (`PersonPublicResponse`). |
| ✅ BUG-002 | Добавить `/persons/**` в permitAll() | BUG-001 | `SecurityConfig` содержит `/persons/**` в списке permitAll. Неавторизованный пользователь может напрямую перейти на `/persons/{id}` без редиректа на `/login`. Страница карточки показывает маскированные данные (фамилия "И*****", год рождения "1990", пустое Био). Кнопка "Редактировать" скрыта. |

---

## Фаза 5.3: Переименование ownerUsername → login

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ REFACTOR-001 | Миграция Flyway `V3__rename_owner_username_to_login.sql` | None | Колонка `owner_username` переименована в `login` в БД. |
| ✅ REFACTOR-002 | Обновить `Person.java` | REFACTOR-001 | Поле `ownerUsername` → `login`. |
| ✅ REFACTOR-003 | Обновить DTO (PersonRequest, PersonResponse, PersonPublicResponse) | REFACTOR-001 | Все DTO содержат `login` вместо `ownerUsername`. |
| ✅ REFACTOR-004 | Обновить `PersonRepository.java` | REFACTOR-001 | `findByOwnerUsername()` → `findByLogin()`. |
| ✅ REFACTOR-005 | Обновить `PersonService.java` | REFACTOR-001 | Все методы используют `login`. |
| ✅ REFACTOR-006 | Обновить `PermissionService.java` | REFACTOR-001 | `isOwner()` использует `person.getLogin()`. |
| ✅ REFACTOR-007 | Обновить тесты | REFACTOR-001 | Все тесты обновлены, сборка проходит. |

---

## Фаза 5.4: Переработка логики построения дерева

**Контекст:** Приложение рассчитано на одну семью — одно дерево. Нет необходимости искать root по `ownerUsername`. Логика авторизации/ролей нужна только для двух вещей:
- Маскировать / не маскировать данные (гости vs авторизованные)
- Разрешать / запрещать редактирование карточек (владение + MASTER-роль)

**Проблема текущей реализации:** `PersonService.getFamilyTree(String rootUsername)` ищет root-персону по `ownerUsername`, что требует хардкода имени пользователя.

**Решение:** Загружать всех людей из БД одним запросом `findAll()`, строить дерево по связям `father`/`spouse`.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| ✅ REFACTOR-008 | Удалить параметр `rootUsername` из `getFamilyTree()` | REFACTOR-007 | `PersonService.getFamilyTree()` загружает `findAll()`, находит root по `father == null`, строит дерево. |
| ✅ REFACTOR-009 | Обновить `TreeController.java` | REFACTOR-008 | `treePage()` вызывает `getFamilyTree()` без параметров. Удалён хардкод `"admin"`/`"root"`. |
| ✅ REFACTOR-010 | Обновить `PersonController.java` | REFACTOR-008 | REST `/api/persons/tree` вызывает `getFamilyTree()` без параметров. |
| ✅ REFACTOR-011 | Удалить `findChildrenPublic()` | REFACTOR-008 | Логика фильтрации детей перенесена в `getFamilyTree()`. |

---

## Фаза 6: Деплой и полировка

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| DEVOPS-017 | Production Dockerfile | (выполнено) | Multi-stage сборка, образ < 200MB. |
| DEVOPS-018 | Deploy на Render.com | DEVOPS-017 | Код пушится в main → деплой на Render. Публичный URL доступен, SSL Let's Encrypt активен. Ключи окружения в секретах сервиса. |

---

## Definition of Done для Epic

❌ Пользователь открывает деплоймент на Render:

1. ✅ Видит главную страницу с визуализацией древа (блоки Муж+Жена связаны с Сыном и Дочерью).
2. ✅ Кликает по любому элементу (например, «Сын»). Открывается карточка с ФИО (фамилия маскирована: «И*****»), датой рождения (только год: «1990»), фото и пустым полем «Био».
3. ✅ Кликает «Войти» в хедере, вводит логин/пароль (`user` / `password`). После авторизации становятся видны полные данные (фамилия, полные даты рождения) всех членов семьи.
4. ✅ Возвращается в карточку пользователя с `login="user"`, нажимает «Редактировать».
5. ✅ Меняет биографию или дату рождения (поля «Фото» в форме нет).
6. ✅ Сохраняет — страница перезагружается, новый текст виден на живом сайте.
7. ✅ Переходит в профиль другого члена семьи — кнопки «Редактировать» нет.
8. ✅ Заходит на `/register` — видит заглушку.
9. ✅ Пытается зайти напрямую на `/persons/{other-person-id}/edit` — получает HTTP 403 Forbidden.
10. ✅ Кликает «Войти» с кредами `admin` / `password` — видит полные данные всех членов семьи и может редактировать **любую** карточку (MASTER-роль bypasses ownership check).
