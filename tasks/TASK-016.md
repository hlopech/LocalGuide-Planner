# TASK-016: Вкладка «Избранное»

**Автор:** Tech Director
**Дата создания:** 2026-03-21
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-3
**Зависимости:** TASK-015

---

## Описание

Добавить третью вкладку «Избранное» в BottomNavBar. Экран показывает только места с `isFavorite == true`, реиспользует уже созданный `AnimatedPlaceCard`. Нет FAB, нет формы добавления — только просмотр и навигация в детали. Задача зависит от TASK-015, потому что та расширяет BottomNavBar до двух вкладок и устанавливает шаблон добавления новых вкладок.

### Целевой дизайн

- Структура экрана аналогична `PlacesListScreen`: `Scaffold` без FAB, `LazyColumn` с `AnimatedPlaceCard`
- Empty state: центрированный `Column` с `Icon(Icons.Rounded.FavoriteBorder)` размером 64.dp цвета `onSurfaceVariant`, текст «Нет избранных мест» (`headlineSmall`), подтекст «Добавляйте места в избранное через карточку места» (`bodyMedium`)
- Карточки кликабельны → навигация на `PlaceDetailScreen` через колбек `onNavigateToDetail(place.id)`
- Кнопка удаления на карточке: при нажатии показывается `Snackbar` с текстом «Чтобы удалить место, перейдите в его карточку» (не удалять из избранного — удаление только через `PlaceDetailScreen`)
- Опционально (по желанию Developer): `FilterChip`-ы с категориями — аналогично TASK-014, но без строки поиска

---

## Предусловия

- [ ] TASK-015 выполнена: BottomNavBar имеет две вкладки, шаблон навигации установлен
- [ ] `AnimatedPlaceCard` существует в `ui/places/common/PlaceCard.kt`
- [ ] `GetPlacesUseCase` возвращает `Flow<List<Place>>`
- [ ] `Screen.PlaceDetail` существует в `Screen.kt`

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `ui/places/favorites/FavoritesUiState.kt` | create | Data class UiState |
| `ui/places/favorites/FavoritesViewModel.kt` | create | HiltViewModel, фильтрация избранных |
| `ui/places/favorites/FavoritesScreen.kt` | create | Compose-экран списка избранных |
| `ui/navigation/Screen.kt` | modify | Добавить `data object Favorites : Screen("places/favorites")` |
| `ui/main/MainScreen.kt` | modify | Добавить третью вкладку «Избранное» в список `bottomNavItems` |

### Архитектурные решения

**FavoritesUiState:**

```kotlin
data class FavoritesUiState(
    val places: List<Place> = emptyList(),
    val selectedCategory: PlaceCategory? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
```

**FavoritesViewModel:**

```kotlin
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
) : ViewModel()
```

Реактивная фильтрация через `combine`:

```kotlin
private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)

val uiState: StateFlow<FavoritesUiState> = combine(
    getPlacesUseCase(),
    _selectedCategory,
) { places, category ->
    val favorites = places.filter { it.isFavorite }
    val filtered = if (category == null) favorites
                   else favorites.filter { it.category == category }
    FavoritesUiState(places = filtered, selectedCategory = category, isLoading = false)
}
.catch { e -> emit(FavoritesUiState(isLoading = false, errorMessage = e.message)) }
.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000),
         initialValue = FavoritesUiState(isLoading = true))

fun onCategorySelected(category: PlaceCategory?) { _selectedCategory.value = category }
```

**FavoritesScreen — структура composable:**

```
FavoritesScreen(onNavigateToDetail, modifier, viewModel)
    ↓ collectAsStateWithLifecycle
FavoritesContent(uiState, onNavigateToDetail, onCategorySelected, modifier)
    ├── Scaffold (без FAB, snackbarHost)
    └── FavoritesBody
        ├── [опционально] PlacesCategoryFilter(selectedCategory, onCategorySelected)
        ├── isLoading → CircularProgressIndicator
        ├── errorMessage != null → ErrorContent
        ├── places.isEmpty() → EmptyFavoritesContent
        └── else → FavoritesList (LazyColumn с AnimatedPlaceCard)
```

**Обработка кнопки удаления на карточке:**

`AnimatedPlaceCard` принимает `onDeleteClick: () -> Unit`. В `FavoritesScreen` передавать:
```kotlin
onDeleteClick = {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(deleteHintText)
    }
}
```

Это информирует пользователя без фактического удаления.

**Добавление вкладки в MainScreen:**

Следовать шаблону из TASK-015. Добавить в список `bottomNavItems`:
```kotlin
BottomNavItem(Screen.Favorites, R.string.nav_favorites, Icons.Rounded.Favorite),
```

Добавить в `NavHost` нового `MainScreen`:
```kotlin
composable(route = Screen.Favorites.route) {
    FavoritesScreen(onNavigateToDetail = onNavigateToDetail)
}
```

Заголовок TopAppBar для вкладки Favorites: `R.string.app_bar_title_favorites`.

**Важно:** `FavoritesScreen` должен получать `onNavigateToDetail: (String) -> Unit` через лямбду из `MainScreen`, которая в свою очередь получает её из `AppNavGraph`. Это сохраняет навигационную архитектуру: навигация выше списка мест выполняется через внешний NavController.

### Строки для добавления в strings.xml

```xml
<string name="nav_favorites">Избранное</string>
<string name="app_bar_title_favorites">Избранное</string>
<string name="favorites_empty_title">Нет избранных мест</string>
<string name="favorites_empty_subtitle">Добавляйте места в избранное через карточку места</string>
<string name="favorites_delete_hint">Чтобы удалить место, перейдите в его карточку</string>
```

### Зависимости (build.gradle.kts)

Новые зависимости не требуются.

## Критерии приёма (Definition of Done)

- [ ] `FavoritesUiState` создан в `ui/places/favorites/`
- [ ] `FavoritesViewModel` создан, фильтрует список по `isFavorite == true`
- [ ] `FavoritesScreen` создан и отображает список избранных мест
- [ ] `Screen.Favorites` добавлен в `Screen.kt`
- [ ] Третья вкладка «Избранное» добавлена в BottomNavBar с иконкой `Icons.Rounded.Favorite`
- [ ] Тап на карточку → навигация на `PlaceDetailScreen`
- [ ] Тап на кнопку удаления → показывается `Snackbar` с подсказкой, место не удаляется
- [ ] Empty state показывается корректно при отсутствии избранных
- [ ] Если место помечено избранным → немедленно появляется во вкладке (реактивность через Flow)
- [ ] Если место снято с избранного → немедленно исчезает из вкладки
- [ ] Навигация сохраняет состояние при переключении вкладок
- [ ] Unit-тесты для `FavoritesViewModel`: фильтрация избранных, фильтр по категории, пустой список
- [ ] `@Preview` для FavoritesScreen: с данными, пустой список
- [ ] Нет hardcoded строк и цветов
- [ ] KTLint без ошибок
- [ ] Функции не длиннее 50 строк

## Тест-сценарии для QA

1. **Отображение избранных:**
   - Добавить несколько мест, часть пометить избранными. Перейти на вкладку «Избранное».
   - Ожидаемый результат: отображаются только избранные места.

2. **Реактивное обновление:**
   - Перейти на «Избранное». В другой вкладке снять место с избранного.
   - Ожидаемый результат: место исчезает из списка избранных без перезагрузки экрана.

3. **Empty state:**
   - Убрать все места из избранного. Перейти на «Избранное».
   - Ожидаемый результат: отображается пустой экран с иконкой сердца и текстом «Нет избранных мест».

4. **Навигация в детали:**
   - Тапнуть на карточку избранного места.
   - Ожидаемый результат: открывается `PlaceDetailScreen` данного места.

5. **Кнопка удаления:**
   - Нажать иконку удаления на карточке в списке избранных.
   - Ожидаемый результат: место не удаляется, появляется `Snackbar` с текстом «Чтобы удалить место, перейдите в его карточку».

6. **Сохранение состояния вкладки:**
   - Перейти на «Избранное», прокрутить список. Переключиться на другую вкладку и вернуться.
   - Ожидаемый результат: список восстановлен на той же позиции (если `rememberLazyListState` + `saveState`).

7. **Фильтр по категории (если реализован):**
   - Выбрать категорию «Парк» на вкладке «Избранное».
   - Ожидаемый результат: показываются только избранные места категории PARK.

---

## Статус выполнения

**Статус:** todo

**Ветка:** feature/TASK-016-favorites-tab (заполняет Developer)
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-21 | todo | Tech Director | Задача создана |
