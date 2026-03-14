# TASK-005: Навигационная инфраструктура

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-003 (Room, DataStore, Repository), TASK-004 (OnboardingScreen)

---

## Описание

Реализация навигационной инфраструктуры приложения LocalGuide Planner.
Включает граф навигации (`AppNavGraph`), модель экранов (`Screen`), главный экран с Bottom Navigation (`MainScreen`), `MainViewModel` для определения стартового экрана, и обновление `MainActivity`.

## Предусловия

- [x] TASK-003 выполнена (Room, DataStore, UseCase инфраструктура)
- [x] TASK-004 выполнена (OnboardingScreen)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `ui/navigation/Screen.kt` | create | Sealed class с маршрутами |
| `ui/navigation/AppNavGraph.kt` | create | NavHost с маршрутами |
| `ui/main/MainViewModel.kt` | create | ViewModel с startDestination |
| `ui/main/MainScreen.kt` | create | Scaffold + BottomNav + вложенный NavHost |
| `MainActivity.kt` | modify | Интеграция с MainViewModel + AppNavGraph |
| `res/values/strings.xml` | modify | Добавить nav_places, app_bar_title_places |
| `gradle/libs.versions.toml` | modify | Добавить material-icons-extended |
| `app/build.gradle.kts` | modify | Добавить material-icons-extended зависимость |

### Архитектурные решения

- `Screen` — sealed class с route-строками для type-safe навигации
- `AppNavGraph` — корневой NavHost, управляет переходами между Onboarding и Main
- `MainScreen` — вложенный NavHost с Bottom Navigation для tab-навигации
- `MainViewModel` — определяет стартовый экран через `IsOnboardingCompletedUseCase`
- После онбординга: `popUpTo(Screen.Onboarding.route) { inclusive = true }` — очищает back stack

## Критерии приёма (Definition of Done)

- [x] `Screen.kt` содержит все маршруты: Onboarding, Main, PlacesList, PlaceDetail, AddPlace, EditPlace
- [x] `AppNavGraph.kt` настраивает NavHost с корректными маршрутами и аргументами
- [x] `MainViewModel` определяет startDestination через `IsOnboardingCompletedUseCase`
- [x] `MainScreen` содержит Scaffold с TopAppBar и BottomNavigationBar (1 вкладка "Места")
- [x] `MainActivity` показывает `CircularProgressIndicator` пока startDestination == null
- [x] Строки `nav_places` и `app_bar_title_places` добавлены в strings.xml
- [x] Unit-тесты для `MainViewModel` написаны и проходят
- [ ] KTLint без ошибок (не проверено — проект не компилируется)
- [ ] Нет hardcoded строк/цветов (нарушено: 4 hardcoded строки в AppNavGraph.kt и MainScreen.kt)

## Тест-сценарии для QA

1. **Онбординг не пройден:** при запуске приложения показывается OnboardingScreen
   - Ожидаемый результат: пользователь видит экран онбординга

2. **Онбординг пройден:** при запуске приложения показывается MainScreen с Places
   - Ожидаемый результат: пользователь видит MainScreen с TopAppBar "Мои места" и BottomNav "Места"

3. **Загрузка startDestination:** пока идёт определение startDestination — показывается индикатор загрузки
   - Ожидаемый результат: CircularProgressIndicator по центру экрана

4. **После онбординга:** навигация из Onboarding в Main очищает back stack
   - Ожидаемый результат: кнопка "Назад" не возвращает на OnboardingScreen

---

## Статус выполнения

**Статус:** fail

**Ветка:** feature/TASK-005-navigation
**PR:** https://github.com/hlopech/LocalGuide-Planner/pull/4
**QA вердикт:** FAIL
**Мерж:** заблокирован

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-14 | in_progress | Developer | Начата реализация |
| 2026-03-14 | review | Developer | Реализация завершена, открыт PR |
| 2026-03-14 | fail | QA Tester | BUG-005 (Critical): Unresolved reference 'OnboardingScreen' — проект не компилируется. BUG-006 (Medium): 4 hardcoded строки в AppNavGraph.kt и MainScreen.kt. Требуется: смержить TASK-004 в develop, затем rebase TASK-005; исправить hardcoded строки. |
