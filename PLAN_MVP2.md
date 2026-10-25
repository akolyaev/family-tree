# Project: Family Tree MVP 2.0 — Cedrine Migration (Spring Boot REST + Cedrine)

**Goal:** Перевести frontend на чистый JS + Cedrine (family tree SVG library). Spring Boot остаётся как REST API backend. Thymeleaf минимизирован (только login/register формы). Граф рисуется Cedrine — без хрупких CSS-коннекторов, без N+1 запросов, без дублирования данных на сервере.

---

## Фаза 1: Frontend scaffold + Vite

**Контекст:** Создаём отдельный `frontend/` проект на Vite + TypeScript. Подключаем Cedrine. Настраиваем Spring Boot для раздачи статки.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-100 | Создать `frontend/` структуру | None | Файлы: `index.html`, `src/main.ts`, `src/tree.ts`, `src/api.ts`, `src/utils.ts`. `package.json` (pnpm), `tsconfig.json`, `vite.config.ts`. `pnpm build` собирает `dist/`. Vite dev server (`pnpm dev`) — опционален для быстрой разработки с HMR; основной доступ через Spring Boot на `:8080`. |
| MVP2-101 | Подключить `@phinnaeus/cedrine` | MVP2-100 | `pnpm i @phinnaeus/cedrine` проходит без ошибок. Import в `tree.ts` работает. |
| MVP2-102 | Spring Boot: раздача SPA static | MVP2-100 | `WebConfig` — `addResourceHandler("/**")` → `classpath:/static/`. SPA fallback: все запросы к несуществующим файлам → `index.html`. |

> ⏸ **Checkpoint 0 — Infra only.** UI ещё нет. Проверяем: `mvn clean package` собирается, `pnpm build` собирает `dist/` без ошибок, Spring Boot раздаёт static с `:8080`. |

---

## Фаза 2: Backend для Cedrine

**Контекст:** Бэкенд возвращает `TreeResponse` (root/wife/children), Cedrine принимает `PersonNode` (персона + partnerships → дети). Нужен слой конвертации + поле gender.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-200 | Добавить `gender` в entity + DTO | None | `Person.gender` (enum, nullable). Flyway `V4__add_gender_to_person.sql`. DTO: `PersonResponse`, `PersonPublicResponse`, `PersonRequest` содержат `gender: String \| null`. |
| MVP2-201 | `TreeConverter` — `TreeResponse → PersonNode` | MVP2-200 | Класс `TreeConverter` с методом `toPersonNode(TreeResponse)`. Корень → `PersonNode.person`, wife → `Partnership.partner`, children → `Partnership.children`. Поддержка `null` wife и пустых children. |
| MVP2-202 | Endpoint `GET /api/persons/tree/personnode` | MVP2-201 | Возвращает `PersonNode` JSON. Авторизованный → полные данные, неавторизованный → `PersonPublicResponse` (masked). |
| MVP2-203 | Endpoint `GET /api/auth/me` | None | Возвращает `{ authenticated: true/false, username, roles }` или 401. Используется navbar для определения статуса. |

> ⏸ **Checkpoint 1 — REST API ready.** UI ещё нет. Проверяем:
> - `curl http://localhost:8080/api/auth/me` → 401 (не авторизован)
> - `curl -u admin:password http://localhost:8080/api/auth/me` → `{ authenticated: true, username: "admin", roles: [...] }`
> - `curl http://localhost:8080/api/persons/tree/personnode` → JSON с PersonNode (masked)
> - `curl -u admin:password http://localhost:8080/api/persons/tree/personnode` → JSON с PersonNode (full)
> - `mvn test` проходит

---

## Фаза 3: SPA Shell

**Контекст:** Один HTML-файл — точка входа SPA. Navbar, контейнер дерева, модальное окно карточки, hash-based routing.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-300 | `frontend/index.html` + navbar | MVP2-100 | Точка входа SPA: navbar (логотип, кнопки "Дерево", "Войти"/"Выйти", search input), `<div id="tree-container">`, `<div id="person-modal">` (скрыто). Подключает `src/main.ts`. |
| MVP2-301 | SPA routing + modal shell | MVP2-300 | Hash-based routing: `#/tree` → показать граф, `#/person/{id}` → открыть modal. `hashchange` listener обновляет view. Modal содержит skeleton карточки и кнопки "Редактировать"/"Закрыть". |

> ⏸ **Checkpoint 2 — SPA Shell visible.** UI появляется! Проверяем:
> - Открываем `/` → видим navbar с логотипом, кнопками "Дерево", "Войти"/"Выйти", search input
> - Контейнер дерева пустой (белый блок) — данные ещё не подключены
> - Modal открыт по умолчанию или по кнопке — видим skeleton карточки
> - Нажимаем "Закрыть" → modal скрывается
> - Нажимаем "Войти" → Spring Security form login (redirect на `/login`)
> - После логина → redirect на `/tree` → SPA routing с `#/tree`
> - Кнопка "Войти" меняется на логин пользователя + "Выйти"
> - **UI доступен, но дерево пустое** — это рабочий инкремент

---

## Фаза 4: Tree rendering с Cedrine

**Контекст:** Основная фича — рендеринг SVG-дерева. Fetch данных, renderCard, avatars, search, fitToView.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-400 | Cedrine init + fetch tree data | MVP2-202 | `src/tree.ts`: `createFamilyTree(container, options)` с кастомным `renderCard` (фото, ФИО, даты). `fetch('/api/persons/tree/personnode')` → `tree.update(data)`. `tree.fitToView()` после загрузки. Error state — "Дерево пусто"/"Ошибка". |
| MVP2-401 | PII masking + avatars + CSS | MVP2-400 | Для неавторизованных: фамилия маскируется, даты → только год. `<img>` для `avatar_url` с placeholder. CSS variables (`--ced-card-bg`, `--ced-card-border`, `--ced-text`) переопределены в `index.html`. Layout: `cardWidth: 160, cardHeight: 80, xSpacing: 40, ySpacing: 120, coupleGap: 16`. |
| MVP2-402 | Search by name | MVP2-401 | Search input в navbar → `tree.search(query)` + `tree.searchHighlight(personIds)`. Очистка при пустом поле. Найденные подсвечиваются жёлтым. |

> ⏸ **Checkpoint 3 — Tree visible.** Проверяем:
> - Открываем `/` → видим SVG-дерево с карточками (Cedrine render)
> - Zoom/pan/drag работают (колесо мыши, drag)
> - Карточки показывают: фото, ФИО (masked для гостей), даты (год для гостей)
> - Клик на карточку → highlight в дереве + connecting path
> - Search input: вводим имя → найденные подсвечиваются жёлтым
> - Неавторизованный пользователь видит masked данные
> - Авторизованный пользователь (`admin`) видит полные данные
> - **Дерево интерактивное, но модалки и редактирование ещё не работают**

---

## Фаза 5: Modal карточки + редактирование

**Контекст:** Клик на карточку → modal с полной инфой. Inline-редактирование в modal → PATCH → дерево обновляется с анимацией.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-500 | Fetch персоны + modal render + highlight | MVP2-301 | `fetch('/api/persons/{id}')` → modal с фото, ФИО, даты, био. `tree.highlight([personId])` при открытии. `tree.clearHighlight()` при закрытии. Кнопка "Редактировать" видна только если `canEdit` (вложен в `PersonResponse`). |
| MVP2-501 | Edit mode + submit + tree update | MVP2-500 | Modal переключается в edit mode: bio (textarea), birthDate/deathDate (date input), firstName/lastName — readonly. `PATCH /api/persons/{id}` с `PersonUpdateRequest`. При успехе — обновить modal + `tree.update(newData)` для анимации дерева. Клиентская валидация (HTML5 required/maxlength). |

> ⏸ **Checkpoint 4 — Full UI.** Проверяем:
> - Клик на карточку → modal открывается с полной инфой (фото, ФИО, даты, био)
> - Кнопка "Редактировать" видна только для owner/MASTER
> - Edit mode: меняем bio → сохраняем → modal обновляется + дерево анимируется
> - Кнопка "Закрыть" → modal закрывается, highlight сбрасывается
> - Переход к чужой карточке → кнопки "Редактировать" нет
> - **Полный UI функционал работает**

---

## Фаза 6: Cleanup + Deploy

**Контекст:** Удаление Thymeleaf, обновление Dockerfile, docs, финальная полировка.

| ID | Задача | Dependencies | Acceptance Criteria |
|----|--------|--------------|---------------------|
| MVP2-600 | Удалить Thymeleaf + обновить SecurityConfig | MVP2-501 | `spring-boot-starter-thymeleaf` удалён из `pom.xml`. CSRF disabled (SPA uses fetch). Form login остаётся для `/login`. `/api/persons/**`, `/api/auth/**`, `/login` — permitAll. Остальное — authenticated. Static HTML для login/register (переименовать `login.html` → `static/login.html`). |
| MVP2-601 | Multi-stage Dockerfile | MVP2-600 | Dockerfile: `pnpm build` для frontend → static в `target/classes/static/`. Образ < 200MB. `corepack enable pnpm`. |
| MVP2-602 | docker-compose + docs | MVP2-601 | `docker-compose up -d --build` собирает и запускает app с SPA. Обновить `QUICKSTART.md`, `API_TESTS.md`, `PLAN.md`. |

> ⏸ **Checkpoint 5 — Deploy ready.** Проверяем:
> - `mvn clean package` собирается
> - `docker-compose up -d --build` запускает app
> - `docker-compose down && docker-compose up -d` — чистый старт работает
> - Все 10 пунктов Definition of Done проходят на локальном деплое

---

## Definition of Done для MVP2

Пользователь открывает деплоймент на Render:

1. Видит главную страницу с интерактивным семейным деревом (Cedrine SVG).
2. Может zoom/pan/drag по дереву.
3. Кликает на карточку — открывается модальное окно с полной информацией.
4. Фамилия и даты маскированы для гостей (как раньше: "И*****", "1990").
5. Кликает "Войти" → вводит логин/пароль → видит полные данные всех.
6. Кликает "Редактировать" → меняет био → сохраняет → дерево обновляется с анимацией.
7. Использует search — карточки найденных подсвечиваются жёлтым.
8. Переходит в профиль другого члена семьи — кнопки "Редактировать" нет.
9. Пытается зайти напрямую на `/persons/{id}/edit` — получает HTTP 403.
10. Заходит с кредами `admin` — может редактировать любую карточку.

---

## Риски и mitigations

| Риск | Mitigation |
|------|-----------|
| **Cedrine data model ≠ TreeResponse** | Написать `TreeConverter` на бэкенде — преобразование в одном месте |
| **PII masking в frontend** | Маскировать на бэкенде (public vs full response) — не полагаться на frontend |
| **Partnership nesting** | Cedrine поддерживает remarriage (multiple partnerships) — использовать для wife/children |
| **Login flow с SPA** | Spring Security form login → redirect to `/tree` → SPA hash routing |
| **Static resources in Spring** | `ResourceHandlerRegistry` + `addResourceLocations` → `classpath:/static/` |
| **CSS conflicts** | Cedrine использует prefix `ced__` — изолированные CSS variables |
| **pnpm в Docker** | Добавить `pnpm` в Dockerfile (`corepack enable pnpm`) |
| **Thymeleaf для login.html** | Переделать `login.html` / `register.html` в статические HTML-файлы в `static/` |
