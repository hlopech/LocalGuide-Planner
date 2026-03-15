# TASK-007: PlaceDetailScreen — экран деталей места

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-002, TASK-003, TASK-005, TASK-006

---

## Описание

Реализовать экран детального просмотра места (`PlaceDetailScreen`). Экран открывается при нажатии на карточку в `PlacesListScreen` и отображает всю информацию о месте: название, описание, категорию, адрес, статус избранного. Экран предоставляет доступ к редактированию (кнопка в `TopAppBar`) и удалению места. После удаления — автоматический возврат к списку.

## Предусловия

- [ ] TASK-002 выполнена: `GetPlaceByIdUseCase`, `DeletePlaceUseCase` созданы
- [ ] TASK-003 выполнена: data-слой работает
- [ ] TASK-005 выполнена: навигация настроена, `Screen.EditPlace`, `Screen.PlaceDetail.ARG_PLACE_ID` определены
- [ ] TASK-006 выполнена: `PlacesListScreen` существует (навигация «назад» ведёт туда)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/places/detail/PlaceDetailUiState.kt` | create | Sealed-класс состояния экрана |
| `ui/places/detail/PlaceDetailViewModel.kt` | create | ViewModel с загрузкой данных по ID |
| `ui/places/detail/PlaceDetailScreen.kt` | create | Compose-экран детального просмотра |

### Архитектурные решения

- `PlaceDetailViewModel` получает `placeId: String` через `SavedStateHandle` (Hilt + Navigation Compose передают аргументы навигации через `SavedStateHandle` автоматически).
- `UiState` реализован как `sealed class` (а не `data class`): `Loading`, `Success(place: Place)`, `Error(message: String)`, `Deleted`. `Deleted` — специальное состояние для инициирования навигации «назад».
- После удаления места ViewModel эмитирует `PlaceDetailUiEffect.NavigateBack` через `SharedFlow` — это предпочтительнее хранения навигационного флага в UiState.
- Переключение `isFavorite` — через отдельный UseCase или напрямую через `SavePlaceUseCase` (с `place.copy(isFavorite = !place.isFavorite)`).
- Экран не хранит копию модели — все данные приходят из `StateFlow<PlaceDetailUiState>`.

### UiState и UiEffect

```kotlin
// ui/places/detail/PlaceDetailUiState.kt
sealed class PlaceDetailUiState {
    data object Loading : PlaceDetailUiState()
    data class Success(val place: Place) : PlaceDetailUiState()
    data class Error(val message: String) : PlaceDetailUiState()
}

sealed class PlaceDetailUiEffect {
    data object NavigateBack : PlaceDetailUiEffect()
    data class NavigateToEdit(val placeId: String) : PlaceDetailUiEffect()
}
```

### ViewModel

```kotlin
// ui/places/detail/PlaceDetailViewModel.kt
@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaceByIdUseCase: GetPlaceByIdUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val savePlaceUseCase: SavePlaceUseCase
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle[Screen.PlaceDetail.ARG_PLACE_ID])

    private val _uiState = MutableStateFlow<PlaceDetailUiState>(PlaceDetailUiState.Loading)
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlaceDetailUiEffect>()
    val uiEffect: SharedFlow<PlaceDetailUiEffect> = _uiEffect.asSharedFlow()

    init {
        loadPlace()
    }

    private fun loadPlace() {
        viewModelScope.launch {
            val place = getPlaceByIdUseCase(placeId)
            _uiState.value = if (place != null) {
                PlaceDetailUiState.Success(place)
            } else {
                PlaceDetailUiState.Error("Место не найдено")
            }
        }
    }

    fun onDeleteClicked() {
        viewModelScope.launch {
            deletePlaceUseCase(placeId)
            _uiEffect.emit(PlaceDetailUiEffect.NavigateBack)
        }
    }

    fun onToggleFavorite() {
        val current = (_uiState.value as? PlaceDetailUiState.Success)?.place ?: return
        viewModelScope.launch {
            savePlaceUseCase(current.copy(isFavorite = !current.isFavorite))
            loadPlace()
        }
    }

    fun onEditClicked() {
        viewModelScope.launch {
            _uiEffect.emit(PlaceDetailUiEffect.NavigateToEdit(placeId))
        }
    }
}
```

### Экран (ключевые требования)

- `Scaffold` с `TopAppBar`:
  - Кнопка «Назад» (`navigationIcon`) — `navController.popBackStack()`
  - Заголовок — название места
  - Action-кнопка «Редактировать» (`Icons.Default.Edit`)
- **Состояние Loading**: `CircularProgressIndicator` по центру.
- **Состояние Success**: вертикальный список секций (используй `Column` + `Spacer`):
  - Секция «Категория» — `SuggestionChip` с названием категории
  - Секция «Адрес» — иконка `Icons.Default.LocationOn` + текст адреса
  - Секция «Описание» — полный текст описания
  - Кнопка «Избранное» — `IconButton` с `Icons.Default.Favorite` / `FavoriteBorder`
  - Кнопка «Удалить» — `OutlinedButton` с иконкой `Delete`, красный цвет
- **Состояние Error**: текст ошибки + кнопка «Назад».
- Диалог подтверждения удаления (`AlertDialog`) перед фактическим удалением.

### Строки для strings.xml (добавить)

```xml
<string name="place_detail_edit">Редактировать</string>
<string name="place_detail_delete">Удалить место</string>
<string name="place_detail_delete_confirm_title">Удалить место?</string>
<string name="place_detail_delete_confirm_message">Это действие нельзя отменить.</string>
<string name="place_detail_favorite_add">Добавить в избранное</string>
<string name="place_detail_favorite_remove">Убрать из избранного</string>
<string name="place_detail_section_category">Категория</string>
<string name="place_detail_section_address">Адрес</string>
<string name="place_detail_section_description">Описание</string>
<string name="place_detail_not_found">Место не найдено</string>
<string name="dialog_confirm">Подтвердить</string>
<string name="dialog_cancel">Отмена</string>
```

## Критерии приёма (Definition of Done)

- [ ] `PlaceDetailScreen` отображает все поля места: название, описание, категорию, адрес, isFavorite
- [ ] Кнопка «Редактировать» в `TopAppBar` инициирует навигацию к `AddEditPlaceScreen` с `placeId`
- [ ] Кнопка «Удалить» показывает `AlertDialog` с подтверждением
- [ ] После подтверждения удаления — место удалено из БД, экран закрывается (popBackStack)
- [ ] Переключение `isFavorite` работает и обновляет UI
- [ ] `PlaceDetailViewModel` получает `placeId` через `SavedStateHandle`
- [ ] `PlaceDetailViewModel` не импортирует классы из `data/`
- [ ] Навигационные события через `SharedFlow<PlaceDetailUiEffect>`, не через UiState
- [ ] Unit-тесты для `PlaceDetailViewModel`: загрузка места, удаление, toggle favorite, несуществующий ID
- [ ] Есть `@Preview` для `PlaceDetailScreen` (Success state)
- [ ] Все строки в `strings.xml`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — открытие деталей:**
   - Нажать на карточку в списке мест.
   - Ожидаемый результат: открывается `PlaceDetailScreen` с корректными данными места.

2. **Счастливый путь — кнопка «Назад»:**
   - На экране деталей нажать кнопку «Назад» в `TopAppBar` или системную кнопку.
   - Ожидаемый результат: возврат на `PlacesListScreen`.

3. **Счастливый путь — удаление с подтверждением:**
   - Нажать «Удалить место» → подтвердить в диалоге.
   - Ожидаемый результат: возврат на список, место отсутствует в списке.

4. **Счастливый путь — редактирование:**
   - Нажать иконку редактирования в `TopAppBar`.
   - Ожидаемый результат: открывается `AddEditPlaceScreen` с предзаполненными данными.

5. **Граничный случай — отмена удаления:**
   - Нажать «Удалить место» → нажать «Отмена» в диалоге.
   - Ожидаемый результат: диалог закрывается, место не удалено, экран остаётся открытым.

6. **Граничный случай — переключение избранного:**
   - Нажать кнопку избранного на экране деталей.
   - Ожидаемый результат: иконка меняется, при открытии этого места снова статус сохранён.

7. **Негативный сценарий — несуществующий ID:**
   - Попытаться открыть экран с несуществующим `placeId`.
   - Ожидаемый результат: отображается сообщение «Место не найдено», без крэша.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-007-place-detail
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-15 | review | Developer | Реализованы PlaceDetailScreen, PlaceDetailViewModel, тесты |
