# TASK-006: PlacesListScreen — список мест с PlacesListViewModel

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-002, TASK-003, TASK-005

---

## Описание

Реализовать экран списка мест (`PlacesListScreen`) — основной экран вкладки «Места» в Bottom Navigation. Экран отображает все сохранённые места в виде прокручиваемого списка карточек. Поддерживает состояния: пустой список, загрузка, список с данными. Кнопка FAB открывает экран добавления нового места. Нажатие на карточку открывает детали места.

## Предусловия

- [ ] TASK-002 выполнена: `GetPlacesUseCase`, `DeletePlaceUseCase` созданы
- [ ] TASK-003 выполнена: `PlacesRepositoryImpl` работает с Room
- [ ] TASK-005 выполнена: навигация настроена, `Screen.PlaceDetail` и `Screen.AddPlace` определены

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/places/list/PlacesListUiState.kt` | create | Data class состояния экрана |
| `ui/places/list/PlacesListViewModel.kt` | create | ViewModel с StateFlow списка мест |
| `ui/places/list/PlacesListScreen.kt` | create | Compose-экран со списком мест |
| `ui/places/list/PlaceCard.kt` | create | Переиспользуемый Composable карточки места |

### Архитектурные решения

- **PlacesListViewModel** подписывается на `GetPlacesUseCase()` (который возвращает `Flow<List<Place>>`). Это обеспечивает реактивное обновление списка при любых изменениях в Room.
- `Flow` из UseCase преобразуется в `StateFlow<PlacesListUiState>` через `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`.
- `SharingStarted.WhileSubscribed(5000)` — стандартный паттерн для ViewModel: Flow активен пока есть подписчики (5 секунд grace period при смене конфигурации).
- Удаление места — через `DeletePlaceUseCase` с `swipe-to-dismiss` жестом или через меню на карточке.
- Навигация (`onNavigateToDetail`, `onNavigateToAdd`) передаётся как лямбды в `PlacesListScreen` — ViewModel не знает о навигации.
- `PlaceCard` — переиспользуемый компонент, не зависит от ViewModel.

### UiState

```kotlin
// ui/places/list/PlacesListUiState.kt
data class PlacesListUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
```

### ViewModel

```kotlin
// ui/places/list/PlacesListViewModel.kt
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase
) : ViewModel() {

    val uiState: StateFlow<PlacesListUiState> = getPlacesUseCase()
        .map { places -> PlacesListUiState(places = places, isLoading = false) }
        .catch { e -> emit(PlacesListUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlacesListUiState(isLoading = true)
        )

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            deletePlaceUseCase(placeId)
        }
    }
}
```

### Экран (ключевые требования)

- Корневой контейнер — `Box` с `Scaffold` и FAB (`FloatingActionButton` с иконкой `+`).
- **Состояние загрузки** (`isLoading = true`): `CircularProgressIndicator` по центру.
- **Пустой список** (`places.isEmpty() && !isLoading`): иллюстрация-заглушка + текст «Нет сохранённых мест» + кнопка «Добавить первое место».
- **Список с данными**: `LazyColumn` с `PlaceCard` для каждого элемента.
- **Ошибка**: `Snackbar` или текстовое сообщение об ошибке.
- `PlaceCard` отображает: имя места, категорию (chip или badge), адрес, иконку избранного.

### PlaceCard (ключевые требования)

```kotlin
@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

Отображает:
- Название (`name`) — `MaterialTheme.typography.titleMedium`
- Категория — `SuggestionChip` или `Badge`
- Адрес (`address`) — `MaterialTheme.typography.bodySmall`, серый цвет
- Иконка избранного (`isFavorite`) — `Icons.Default.Favorite` / `Icons.Default.FavoriteBorder`
- Кнопка удаления (иконка `Icons.Default.Delete`)

### Строки для strings.xml (добавить)

```xml
<string name="places_list_empty_title">Нет сохранённых мест</string>
<string name="places_list_empty_action">Добавить первое место</string>
<string name="places_list_fab_add">Добавить место</string>
<string name="place_card_delete">Удалить</string>
<string name="places_error_loading">Не удалось загрузить места</string>
```

## Критерии приёма (Definition of Done)

- [ ] `PlacesListScreen` отображается как содержимое вкладки «Места» в `MainScreen`
- [ ] При пустой базе данных показывается соответствующий экран-заглушка
- [ ] При наличии мест отображается `LazyColumn` с карточками
- [ ] Нажатие на карточку вызывает `onNavigateToDetail(place.id)`
- [ ] FAB нажатие вызывает `onNavigateToAdd()`
- [ ] Удаление места через `PlacesListViewModel.deletePlace()` работает и обновляет список
- [ ] `PlacesListViewModel` не импортирует классы из `data/`
- [ ] `stateIn` использует `SharingStarted.WhileSubscribed(5_000)`
- [ ] Unit-тесты для `PlacesListViewModel`: проверка состояний (loading → list), проверка `deletePlace`
- [ ] Есть `@Preview` для `PlacesListScreen` (с данными и пустое состояние)
- [ ] Есть `@Preview` для `PlaceCard`
- [ ] Все строки в `strings.xml`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — список с местами:**
   - Предварительно добавить несколько мест через другой тест или напрямую в Room.
   - Открыть вкладку «Места».
   - Ожидаемый результат: все места отображаются в `LazyColumn` с корректными данными.

2. **Счастливый путь — переход к деталям:**
   - Нажать на карточку места в списке.
   - Ожидаемый результат: открывается `PlaceDetailScreen` с данными этого места.

3. **Счастливый путь — переход к добавлению:**
   - Нажать FAB.
   - Ожидаемый результат: открывается `AddEditPlaceScreen` в режиме добавления.

4. **Граничный случай — пустой список:**
   - Убедиться, что в базе нет мест.
   - Открыть вкладку «Места».
   - Ожидаемый результат: отображается заглушка с текстом «Нет сохранённых мест».

5. **Граничный случай — удаление места:**
   - Нажать кнопку удаления на карточке.
   - Ожидаемый результат: место исчезает из списка, список обновляется реактивно.

6. **Граничный случай — реактивное обновление:**
   - Добавить новое место через `AddEditPlaceScreen`.
   - Вернуться на список.
   - Ожидаемый результат: новое место появляется в списке без перезагрузки экрана.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-006-places-list
**PR:** https://github.com/hlopech/LocalGuide-Planner/pull/6
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-15 | review | Developer | Реализация завершена: PlacesListUiState, PlacesListViewModel, PlacesListScreen, PlaceCard, тесты зелёные (6/6), lintDebug успешен |
