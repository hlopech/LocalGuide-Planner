# TASK-014: Поиск и фильтры в PlacesListScreen

**Автор:** Tech Director
**Дата создания:** 2026-03-21
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-3
**Зависимости:** TASK-013

---

## Описание

Пользователь не может быстро найти нужное место в длинном списке. Необходимо добавить поиск по названию и адресу, а также фильтрацию по категории. Это первая задача Sprint 3 — не изменяет навигацию или BottomNavBar, только расширяет существующий экран `PlacesListScreen`.

### Целевой дизайн

- В верхней части экрана (до списка) — Material3 `SearchBar` в режиме `DockedSearchBar` или обычный `SearchBar` с `active = false` (поле ввода без раскрытия оверлея). При тапе становится активным и принимает ввод.
- Под поисковой строкой — горизонтальный `LazyRow` с `FilterChip`-ами: «Все» + по одному на каждую из 7 категорий `PlaceCategory`.
- Активный фильтр категории визуально выделен (`selected = true`).
- Поиск и фильтр работают одновременно (AND-логика: список показывает только места, которые соответствуют и строке поиска, и выбранной категории).
- При пустом результате фильтрации показывается отдельный `EmptySearchContent` (иконка `Icons.Rounded.SearchOff`, текст «Ничего не найдено»).
- При смене фильтра список перерисовывается через уже существующую `animateItem()` анимацию на LazyColumn.

---

## Предусловия

- [ ] TASK-013 выполнена: `LocalGuideTextField`, `PlaceCategoryExt` (`toStringRes()`, `toIcon()`, `toAccentColor()`) доступны
- [ ] `PlacesListViewModel`, `PlacesListUiState`, `PlacesListScreen` существуют и компилируются
- [ ] `GetPlacesUseCase` возвращает `Flow<List<Place>>`

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `ui/places/list/PlacesListUiState.kt` | modify | Добавить поля `searchQuery: String = ""` и `selectedCategory: PlaceCategory? = null` |
| `ui/places/list/PlacesListViewModel.kt` | modify | Переработать логику: хранить `searchQuery` и `selectedCategory` как `MutableStateFlow`, комбинировать через `combine` с `getPlacesUseCase()`, применять фильтрацию в `.map {}` |
| `ui/places/list/PlacesListScreen.kt` | modify | Добавить `PlacesSearchBar` и `PlacesCategoryFilter` composable-ы в `PlacesListBody` выше списка |

### Архитектурные решения

**ViewModel — реактивная фильтрация через `combine`:**

Текущий `uiState` строится как один `stateIn` поток от `getPlacesUseCase()`. Необходимо переработать на `combine` трёх потоков:

```kotlin
private val _searchQuery = MutableStateFlow("")
private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)

val uiState: StateFlow<PlacesListUiState> = combine(
    getPlacesUseCase(),
    _searchQuery,
    _selectedCategory,
) { places, query, category ->
    val filtered = places.filter { place ->
        val matchesQuery = query.isBlank() ||
            place.name.contains(query, ignoreCase = true) ||
            place.address.contains(query, ignoreCase = true)
        val matchesCategory = category == null || place.category == category
        matchesQuery && matchesCategory
    }
    PlacesListUiState(
        places = filtered,
        searchQuery = query,
        selectedCategory = category,
        isLoading = false,
    )
}
.catch { e -> emit(PlacesListUiState(isLoading = false, errorMessage = e.message)) }
.stateIn(...)
```

Функции обновления состояния:
```kotlin
fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
fun onCategorySelected(category: PlaceCategory?) { _selectedCategory.value = category }
```

**UI — composable-ы:**

- `PlacesSearchBar(query, onQueryChange, modifier)` — Material3 `SearchBar` с `query`, `onQueryChange`, `active = false`, `onActiveChange = {}`, `placeholder = stringResource(R.string.places_search_placeholder)`, `leadingIcon = Icons.Rounded.Search`. Параметр `modifier` обязателен.
- `PlacesCategoryFilter(selectedCategory, onCategorySelected, modifier)` — `LazyRow` с первым элементом «Все» (`FilterChip`, `selected = selectedCategory == null`, `onClick = { onCategorySelected(null) }`), затем по одному `FilterChip` для каждой `PlaceCategory.entries`. Иконки через `category.toIcon()`, строки через `category.toStringRes()`.
- `EmptySearchContent(modifier)` — отдельный composable для пустого результата поиска (отличается от `EmptyPlacesContent` тем, что не содержит кнопку добавления).

**Разделение состояний в `PlacesListBody`:**

```
uiState.isLoading           → CircularProgressIndicator
uiState.errorMessage != null → ErrorContent
uiState.places.isEmpty() && uiState.searchQuery.isBlank() && uiState.selectedCategory == null
                             → EmptyPlacesContent (нет мест вообще)
uiState.places.isEmpty()    → EmptySearchContent (фильтр дал пустой результат)
else                         → PlacesList
```

Поиск и фильтр рендерятся **всегда** когда `!uiState.isLoading && uiState.errorMessage == null` — даже при пустом результате, чтобы пользователь мог сбросить фильтр.

**Важно:** фильтрация — только на уровне ViewModel. Room DAO и Repository **не изменяются**.

### Строки для добавления в strings.xml

```xml
<string name="places_search_placeholder">Поиск по названию или адресу</string>
<string name="places_filter_all">Все</string>
<string name="places_search_empty_title">Ничего не найдено</string>
<string name="places_search_empty_subtitle">Попробуйте изменить запрос или фильтр</string>
```

### Зависимости (build.gradle.kts)

Новые зависимости не требуются. `SearchBar` входит в `androidx.compose.material3`.

## Критерии приёма (Definition of Done)

- [ ] `PlacesListUiState` содержит поля `searchQuery: String` и `selectedCategory: PlaceCategory?`
- [ ] `PlacesListViewModel` использует `combine` и корректно фильтрует список
- [ ] `onSearchQueryChanged(query)` и `onCategorySelected(category)` реализованы в ViewModel
- [ ] `PlacesSearchBar` отображается в `PlacesListScreen` над списком, принимает ввод
- [ ] Поиск фильтрует по `name` и `address` без учёта регистра
- [ ] `PlacesCategoryFilter` показывает кнопку «Все» + 7 категорий с иконками
- [ ] При выборе категории список немедленно фильтруется
- [ ] Поиск + категория работают одновременно (AND-логика)
- [ ] При пустом результате показывается `EmptySearchContent` (не `EmptyPlacesContent`)
- [ ] При полностью пустом списке (без фильтра) показывается `EmptyPlacesContent` с кнопкой добавления
- [ ] Room DAO, Repository, UseCase не изменены
- [ ] Unit-тесты для `PlacesListViewModel` написаны: фильтрация по запросу, по категории, AND-логика, сброс фильтра
- [ ] `@Preview` для состояний: с поиском, с активным фильтром, пустой результат — добавлены/обновлены
- [ ] Нет hardcoded строк
- [ ] KTLint без ошибок
- [ ] Функции не длиннее 50 строк

## Тест-сценарии для QA

1. **Поиск по названию:**
   - Открыть список с несколькими местами. Ввести часть названия одного места.
   - Ожидаемый результат: список немедленно фильтруется, показывает только совпадающие места.

2. **Поиск по адресу:**
   - Ввести название улицы в строку поиска.
   - Ожидаемый результат: показываются места, в адресе которых есть введённая строка.

3. **Фильтр по категории:**
   - Тапнуть на `FilterChip` «Парк».
   - Ожидаемый результат: список показывает только места категории PARK. Чип «Парк» выделен.

4. **Комбинированный фильтр:**
   - Выбрать категорию «Кафе», ввести часть названия.
   - Ожидаемый результат: отображаются только кафе, в названии или адресе которых есть введённая строка.

5. **Сброс фильтра:**
   - Выбрав категорию, нажать «Все».
   - Ожидаемый результат: показываются все места, чип «Все» выделен.

6. **Пустой результат поиска:**
   - Ввести строку, которой нет ни в одном названии или адресе.
   - Ожидаемый результат: показывается `EmptySearchContent` с иконкой и текстом «Ничего не найдено». Строка поиска и фильтры остаются видимыми.

7. **Пустой список мест:**
   - При отсутствии мест в базе без фильтров.
   - Ожидаемый результат: показывается `EmptyPlacesContent` с кнопкой «Добавить первое место».

8. **Регистронезависимость:**
   - Ввести название заглавными буквами.
   - Ожидаемый результат: поиск работает независимо от регистра.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-014-search-and-filters
**PR:** https://github.com/hlopech/LocalGuide-Planner/pull/16
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-21 | todo | Tech Director | Задача создана |
| 2026-03-21 | review | Developer | Реализация завершена, PR открыт |
