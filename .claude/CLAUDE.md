# LocalGuide Planner — Проектное соглашение

> Этот файл читается **всеми агентами** при каждом старте сессии.
> Изменять только Orchestrator-агент.

---

## О проекте

**LocalGuide Planner** — Android-приложение для планирования маршрутов и прогулок по местным достопримечательностям. Пользователи открывают POI (точки интереса), создают персональные планы поездок, сохраняют избранные места и получают рекомендации.

**Корневая папка проекта:** `D:/LocalGuide Planner/`
**Android-проект:** `android core/` (Kotlin, Jetpack Compose)
**Пакет:** `com.example.localguide_planner`

---

## Стек технологий

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin (minSdk 24, targetSdk 36) |
| UI | Jetpack Compose + Material3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt |
| Навигация | Compose Navigation |
| Сеть | Retrofit + OkHttp |
| БД | Room |
| Сборка | Gradle KTS, AGP 9.0.1, Kotlin 2.0.21 |
| Тесты | JUnit4, MockK, AndroidX Test, Espresso |
| Линтер | KTLint + Detekt |

---

## Структура репозитория

```
/LocalGuide Planner/
├── android core/               # Android-проект
│   └── app/src/main/java/
│       └── com/example/localguide_planner/
│           ├── data/           # Repository + Room + Retrofit
│           ├── domain/         # UseCase + Models
│           ├── ui/             # Compose Screens + ViewModels
│           └── di/             # Hilt modules
├── tasks/                      # Задачи (TASK-NNN.md)
├── bugs/                       # Баг-репорты (BUG-NNN.md)
├── docs/
│   └── arch/                   # Архитектурные решения
├── project_state.json          # Состояние проекта
└── .claude/
    ├── CLAUDE.md               # Этот файл
    └── agents/                 # Системные промпты агентов
```

---

## Архитектурные правила (MVVM + Clean)

```
Screen (Compose) → ViewModel → UseCase → Repository → DataSource
```

- **UI-слой:** `ui/<feature>/<Feature>Screen.kt` + `<Feature>ViewModel.kt`
- **Domain:** `domain/usecase/<Action><Entity>UseCase.kt`, `domain/model/<Entity>.kt`
- **Data:** `data/repository/<Entity>RepositoryImpl.kt`, `data/local/` (Room), `data/remote/` (Retrofit)
- **DI:** один Hilt-модуль на слой (`NetworkModule`, `DatabaseModule`, `RepositoryModule`)
- ViewModel **не** импортирует классы из `data/`; только через интерфейс UseCase/Repository
- Никаких `LiveData` — только `StateFlow` / `SharedFlow`

---

## Стандарты кода

- Только Kotlin — Java-файлы запрещены
- UI только через Jetpack Compose — XML-layouts запрещены
- Именование:
  - Классы/объекты: `PascalCase`
  - Функции/переменные: `camelCase`
  - Константы: `UPPER_SNAKE_CASE`
  - Файлы ресурсов: `snake_case`
- Строки только через `strings.xml`, цвета через `Color.kt`, нет hardcoded values
- Никаких секретов и API-ключей в коде (используй `local.properties`)
- Каждый публичный Composable должен иметь `@Preview`
- Функции не длиннее 50 строк — рефакторить при превышении

---

## Git Workflow (Gitflow)

```
main ──────────────────────────────────── production
  └── develop ─────────────────────────── интеграция
        ├── feature/TASK-NNN-короткое-имя  Developer
        ├── bugfix/BUG-NNN-описание        Developer
        └── release/x.y.z                  Orchestrator
```

### Правила веток
- `main` и `develop` — **защищённые**, только через PR
- `feature/*` создаёт Developer от `develop`
- `bugfix/*` создаёт Developer от `develop` (или от `main` для hotfix)
- `release/*` создаёт Orchestrator

### Правила PR
1. PR → `develop` (feature) или `main` (release/hotfix)
2. CI обязателен: build ✅ + lint ✅ + tests ✅
3. Code review: Tech Director или QA
4. Merge только Orchestrator

### Naming conventions
- `feature/TASK-001-login-screen`
- `bugfix/BUG-012-null-pointer-on-map`
- `release/1.0.0`

---

## Роли агентов

### Orchestrator
- Координирует команду, управляет задачами и статусами
- Единственный, кто мержит PR и обновляет `project_state.json`
- **Нельзя:** писать код, запускать тесты, изменять код без задачи

### Tech Director
- Проектирует архитектуру, создаёт задачи в `tasks/TASK-NNN.md`
- Пишет в `docs/arch/`, редактирует `tasks/`
- **Нельзя:** писать production-код, мержить, тестировать

### Developer
- Реализует задачи из `tasks/`, создаёт ветки `feature/TASK-*`
- Пишет код в `android core/`, добавляет тесты, открывает PR
- **Нельзя:** работать вне своей задачи, мержить без review, писать в `bugs/`

### QA Tester
- Тестирует PR: unit/UI-тесты + ручная проверка + статический анализ
- Создаёт `bugs/BUG-NNN.md`, выдаёт вердикт PASS/FAIL
- **Нельзя:** изменять production-код напрямую, закрывать задачи без проверки

---

## Права доступа к файлам

| Файл/Папка | Orchestrator | Tech Director | Developer | QA Tester |
|------------|:---:|:---:|:---:|:---:|
| `tasks/*.md` | R/W | R/W | R | R |
| `android core/` | R (merge) | R | R/W | R |
| `bugs/*.md` | R | R | R | R/W |
| `docs/arch/` | R | R/W | R | R |
| `project_state.json` | R/W | — | — | — |
| `CLAUDE.md` | R/W | R | R | R |
| Git `feature/*` | — | — | create/push | — |
| Git merge | ✅ | — | — | — |

---

## Quality Gates (перед мержем)

- [ ] Компилируется без ошибок
- [ ] Все unit-тесты зелёные (`./gradlew test`)
- [ ] KTLint без ошибок (`./gradlew ktlintCheck`)
- [ ] Нет критических замечаний от QA
- [ ] PR описывает изменения и ссылается на TASK-NNN

---

## Правила принятия решений

1. **3 фикса подряд не решают баг** → Tech Director пересматривает архитектуру
2. **Критический баг** (крэш, потеря данных) → немедленно уведомить Orchestrator
3. **Задача заблокирована** → создать запись `BLOCKED` в задаче, уведомить Orchestrator
4. **Конфликт веток** → Developer разрешает конфликт вручную, при необходимости консультируется с Tech Director
5. **Нарушение архитектурных правил** → Tech Director выдаёт замечание, Developer исправляет до мержа

---

## Форматы ID

- Задачи: `TASK-001`, `TASK-002`, ... (нумерация по порядку)
- Баги: `BUG-001`, `BUG-002`, ...
- Спринты: `SPRINT-1`, `SPRINT-2`, ...
