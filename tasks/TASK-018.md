# TASK-018: Bugfix Sprint — устранение накопленного технического долга

**Автор:** Tech Director
**Дата создания:** 2026-03-25
**Тип:** bugfix
**Приоритет:** High
**Спринт:** SPRINT-4
**Зависимости:** нет

---

## Описание

Задача закрывает 6 открытых багов, накопленных за Sprint 2–3. Все исправления сгруппированы в три логических блока по файлам. Ни один баг не является критическим (краш / потеря данных), но совокупно они нарушают архитектурные стандарты проекта и создают технический долг.

**Закрываемые баги:** BUG-029, BUG-030, BUG-032, BUG-033, BUG-034, BUG-037.

## Предусловия

- [ ] `develop` актуален (все Sprint-3 задачи смержены)
- [ ] TASK-014, TASK-013, TASK-015 смержены — исходный код для исправления доступен

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `ui/places/list/PlacesListScreen.kt` | modify | BUG-032, BUG-033, BUG-034: миграция SearchBar API, подъём состояния active, рефакторинг функций |
| `ui/places/list/PlacesListViewModel.kt` | modify | BUG-032: добавить поле `isSearchActive: Boolean` в логику ViewModel |
| `ui/places/list/PlacesListUiState.kt` | modify | BUG-032: добавить поле `isSearchActive: Boolean = false` |
| `ui/places/addedit/AddEditPlaceScreen.kt` | modify | BUG-029, BUG-030: рефакторинг функции, замена иконки |
| `ui/profile/ProfileViewModel.kt` | modify | BUG-037: убрать side effect из лямбды combine |

### Архитектурные решения

#### Блок 1 — PlacesListScreen.kt (BUG-032, BUG-033, BUG-034)

**BUG-033 (deprecated SearchBar API).**
Мигрировать с устаревшего overload на актуальный Material3 API:

```kotlin
// БЫЛО (deprecated):
SearchBar(
    query = query,
    onQueryChange = onQueryChange,
    onSearch = { active = false },
    active = active,
    onActiveChange = { active = it },
    ...
)

// СТАЛО (актуальный API):
SearchBar(
    inputField = {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onActiveChange(false) },
            expanded = expanded,
            onExpandedChange = onActiveChange,
            placeholder = { Text(stringResource(R.string.places_search_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = null) } }
            } else null,
        )
    },
    expanded = expanded,
    onExpandedChange = onActiveChange,
    modifier = modifier.padding(horizontal = 16.dp),
) { /* suggestions — пусто */ }
```

**BUG-032 (локальное состояние active).**
Поднять `active` в `PlacesListUiState` + `PlacesListViewModel`:

```kotlin
// PlacesListUiState.kt — добавить поле:
data class PlacesListUiState(
    ...
    val isSearchActive: Boolean = false,
)

// PlacesListViewModel.kt — добавить метод:
fun onSearchActiveChanged(isActive: Boolean) {
    // обновить isSearchActive через отдельный MutableStateFlow или напрямую в combine
}

// PlacesListScreen.kt — PlacesSearchBar становится stateless:
// убрать: var active by remember { mutableStateOf(false) }
// параметры: expanded: Boolean, onExpandedChange: (Boolean) -> Unit
```

**BUG-034 (PlacesListContent > 50 строк).**
`PlacesListContent` сейчас 51 строка — после добавления поля `isSearchActive` вырастет ещё. Вынести `SnackbarHost` блок и `Scaffold` FAB блок в private composable-функции:

```kotlin
// Выделить:
@Composable
private fun PlacesListFab(isScrolled: Boolean, onNavigateToAdd: () -> Unit) { ... }
```

#### Блок 2 — AddEditPlaceScreen.kt (BUG-029, BUG-030)

**BUG-030 (неверная иконка).**
Заменить `Icons.Rounded.Edit` на `Icons.Rounded.Label` (или `Icons.Rounded.Place`) для leading icon поля названия места.

**BUG-029 (функция > 65 строк).**
Вынести секции формы в отдельные private composable:

```kotlin
// Выделить из AddEditPlaceFormContent:
@Composable
private fun PlaceNameField(name: String, onNameChange: (String) -> Unit) { ... }

@Composable
private fun PlaceCategorySelector(
    selectedCategory: PlaceCategory,
    onCategoryChange: (PlaceCategory) -> Unit,
) { ... }
```

#### Блок 3 — ProfileViewModel.kt (BUG-037)

Убрать side effect `_latestProfile.value = profile` из трансформационной лямбды `combine`. Корректное решение — добавить отдельную коллекцию профиля через `onEach`:

```kotlin
// Вариант A (рекомендуемый) — отдельный stateIn для профиля:
private val _profileFlow = getUserProfileUseCase()
    .onEach { _latestProfile.value = it }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

// combine теперь использует _profileFlow вместо getUserProfileUseCase():
val uiState = combine(
    _profileFlow,
    getPlacesUseCase(),
    _editedName,
    _isSaving,
    _showResetDialog,
) { profile, places, editedName, isSaving, showDialog ->
    // нет side effects — только transform
    ProfileUiState(...)
}.stateIn(...)
```

### API / База данных

Изменений схемы Room нет.

## Критерии приёма (Definition of Done)

- [ ] Deprecated `SearchBar` overload заменён на актуальный `SearchBar(inputField = {...})` — компилятор не выдаёт `@Deprecated` предупреждение
- [ ] Состояние `active`/`expanded` SearchBar вынесено из `remember` в `PlacesListUiState.isSearchActive`
- [ ] `PlacesListViewModel` содержит метод управления `isSearchActive`
- [ ] Ни одна функция в `PlacesListScreen.kt` не превышает 50 строк
- [ ] `AddEditPlaceFormContent` разбита на private composable — ни одна функция не превышает 50 строк
- [ ] Leading icon поля "Название" в `AddEditPlaceScreen` — `Icons.Rounded.Label` (не `Edit`)
- [ ] Лямбда `combine` в `ProfileViewModel` не содержит записей в `MutableStateFlow` (нет side effects)
- [ ] `_latestProfile` корректно обновляется через `onEach` вне `combine`
- [ ] Unit-тесты `PlacesListViewModelTest` обновлены под новый метод `onSearchActiveChanged`
- [ ] `./gradlew test` зелёный
- [ ] `./gradlew ktlintCheck` без ошибок
- [ ] Нет hardcoded строк/цветов
- [ ] Каждый изменённый публичный Composable имеет `@Preview`

## Тест-сценарии для QA

1. **SearchBar — смена конфигурации:**
   - Открыть список мест, тапнуть на SearchBar (он раскрывается).
   - Повернуть устройство (смена конфигурации).
   - Ожидаемый результат: SearchBar остаётся раскрытым (состояние не потеряно), потому что оно в ViewModel.

2. **SearchBar — нет deprecated предупреждений:**
   - Запустить `./gradlew :app:compileDebugKotlin`.
   - Ожидаемый результат: нет строк `'SearchBar(query: String, ...)' is deprecated` в выводе.

3. **AddEditPlaceScreen — иконка названия:**
   - Открыть экран добавления/редактирования места.
   - Ожидаемый результат: поле "Название" имеет иконку Label (ценник/метка), а не Edit (карандаш).

4. **ProfileViewModel — side effect:**
   - Открыть экран профиля, изменить имя и нажать "Сохранить".
   - Ожидаемый результат: имя сохраняется корректно; в `ProfileViewModel.kt` отсутствует присваивание внутри лямбды combine (проверяется code review).

5. **Негативный сценарий — SearchBar очистка:**
   - Ввести текст в SearchBar, нажать кнопку X.
   - Ожидаемый результат: поле очищается, SearchBar остаётся раскрытым (не закрывается).

---

## Статус выполнения

**Статус:** review

**Ветка:** bugfix/TASK-018-sprint4-bugfix
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-25 | todo | Tech Director | Задача создана. Закрывает BUG-029, 030, 032, 033, 034, 037 |
| 2026-03-25 | review | Developer | Реализованы все исправления: SearchBar API мигрирован, isSearchActive поднят в ViewModel, иконка Label, ProfileViewModel side effect устранён |
