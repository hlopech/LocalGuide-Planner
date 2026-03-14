# TASK-008: AddEditPlaceScreen — добавление и редактирование места

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-002, TASK-003, TASK-005, TASK-006

---

## Описание

Реализовать экран добавления и редактирования места (`AddEditPlaceScreen`). Экран работает в двух режимах: создание нового места (без `placeId`) и редактирование существующего (с `placeId`). В режиме редактирования форма предзаполняется данными из базы. После сохранения — возврат к предыдущему экрану.

Это последний экран Sprint 1, который закрывает базовый CRUD-цикл для мест.

## Предусловия

- [ ] TASK-002 выполнена: `SavePlaceUseCase`, `GetPlaceByIdUseCase` созданы
- [ ] TASK-003 выполнена: data-слой работает
- [ ] TASK-005 выполнена: навигация настроена, `Screen.AddPlace` и `Screen.EditPlace` определены
- [ ] TASK-006 выполнена: `PlacesListScreen` существует (возврат после сохранения)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/places/addedit/AddEditPlaceUiState.kt` | create | Data class состояния формы |
| `ui/places/addedit/AddEditPlaceViewModel.kt` | create | ViewModel с логикой формы и сохранения |
| `ui/places/addedit/AddEditPlaceScreen.kt` | create | Compose-экран с формой |

### Архитектурные решения

- Один экран для добавления и редактирования — режим определяется наличием `placeId` в `SavedStateHandle`.
- `AddEditPlaceViewModel` получает `placeId: String?` из `SavedStateHandle`. Если `null` — режим создания; если задан — режим редактирования, форма предзаполняется.
- Каждое поле формы — отдельная переменная в `UiState`. Это обеспечивает гранулярную валидацию каждого поля.
- Валидация: поля `name` и `address` — обязательные (не пустые после trim). `description` — опциональное. `latitude`/`longitude` — опциональные, но если введены — должны быть корректными числами в допустимом диапазоне.
- Кнопка «Сохранить» недоступна при наличии ошибок валидации.
- После успешного сохранения — `AddEditPlaceUiEffect.NavigateBack` через `SharedFlow`.
- `SavePlaceUseCase` сам генерирует UUID для новых мест (логика в UseCase, не во ViewModel).

### UiState и UiEffect

```kotlin
// ui/places/addedit/AddEditPlaceUiState.kt
data class AddEditPlaceUiState(
    val name: String = "",
    val nameError: String? = null,
    val description: String = "",
    val category: PlaceCategory = PlaceCategory.OTHER,
    val address: String = "",
    val addressError: String? = null,
    val latitudeInput: String = "",
    val longitudeInput: String = "",
    val coordinatesError: String? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val screenTitle: String = ""   // "Новое место" или "Редактировать место"
)

sealed class AddEditPlaceUiEffect {
    data object NavigateBack : AddEditPlaceUiEffect()
}
```

### ViewModel

```kotlin
// ui/places/addedit/AddEditPlaceViewModel.kt
@HiltViewModel
class AddEditPlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaceByIdUseCase: GetPlaceByIdUseCase,
    private val savePlaceUseCase: SavePlaceUseCase
) : ViewModel() {

    // placeId присутствует только в маршруте EditPlace
    private val placeId: String? = savedStateHandle[Screen.EditPlace.ARG_PLACE_ID]

    private val _uiState = MutableStateFlow(
        AddEditPlaceUiState(
            isEditMode = placeId != null,
            screenTitle = if (placeId != null) "Редактировать место" else "Новое место"
        )
    )
    val uiState: StateFlow<AddEditPlaceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AddEditPlaceUiEffect>()
    val uiEffect: SharedFlow<AddEditPlaceUiEffect> = _uiEffect.asSharedFlow()

    init {
        if (placeId != null) loadPlace(placeId)
    }

    private fun loadPlace(id: String) { /* загрузить и предзаполнить форму */ }

    fun onNameChanged(name: String) { /* обновить name, сбросить nameError */ }
    fun onDescriptionChanged(description: String) { /* обновить description */ }
    fun onCategorySelected(category: PlaceCategory) { /* обновить category */ }
    fun onAddressChanged(address: String) { /* обновить address, сбросить addressError */ }
    fun onLatitudeChanged(latitude: String) { /* обновить latitudeInput */ }
    fun onLongitudeChanged(longitude: String) { /* обновить longitudeInput */ }
    fun onFavoriteToggled() { /* переключить isFavorite */ }

    fun onSaveClicked() {
        if (!validate()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            savePlaceUseCase(buildPlace())
            _uiEffect.emit(AddEditPlaceUiEffect.NavigateBack)
        }
    }

    private fun validate(): Boolean { /* проверить все поля, вернуть false при ошибке */ }
    private fun buildPlace(): Place { /* собрать Place из полей формы */ }
}
```

### Экран (ключевые требования)

- `Scaffold` с `TopAppBar`:
  - Кнопка «Назад» (`navigationIcon`) — `navController.popBackStack()`
  - Заголовок из `uiState.screenTitle`
  - Action-кнопка «Сохранить» (`Icons.Default.Check`) — неактивна при `isLoading` или ошибках
- Форма в `Column` с `verticalScroll`:
  - **Название** — `OutlinedTextField`, обязательное, отображение `nameError`
  - **Описание** — `OutlinedTextField`, многострочный (`minLines = 3`), опциональное
  - **Категория** — выпадающий список через `ExposedDropdownMenuBox` + `DropdownMenuItem` для каждого `PlaceCategory`
  - **Адрес** — `OutlinedTextField`, обязательное, отображение `addressError`
  - **Широта** — `OutlinedTextField`, опциональное, тип клавиатуры `KeyboardType.Decimal`
  - **Долгота** — `OutlinedTextField`, опциональное, тип клавиатуры `KeyboardType.Decimal`
  - **Избранное** — `Row` с текстом «Добавить в избранное» и `Switch`
- При `isLoading = true` кнопка «Сохранить» показывает `CircularProgressIndicator`
- Навигационные события от `uiEffect` обрабатываются в `LaunchedEffect`

### Валидация полей

| Поле | Правило | Сообщение об ошибке |
|------|---------|---------------------|
| `name` | Не пустое после trim, max 100 символов | «Введите название» / «Название слишком длинное» |
| `address` | Не пустое после trim, max 200 символов | «Введите адрес» / «Адрес слишком длинный» |
| `latitude` | Если введено: `Double`, от -90.0 до 90.0 | «Некорректная широта» |
| `longitude` | Если введено: `Double`, от -180.0 до 180.0 | «Некорректная долгота» |
| `latitude`/`longitude` | Оба введены или оба пусты | «Введите обе координаты или оставьте оба поля пустыми» |

### Строки для strings.xml (добавить)

```xml
<string name="add_place_title">Новое место</string>
<string name="edit_place_title">Редактировать место</string>
<string name="place_form_name_label">Название*</string>
<string name="place_form_description_label">Описание</string>
<string name="place_form_category_label">Категория</string>
<string name="place_form_address_label">Адрес*</string>
<string name="place_form_latitude_label">Широта</string>
<string name="place_form_longitude_label">Долгота</string>
<string name="place_form_favorite_label">Добавить в избранное</string>
<string name="place_form_save">Сохранить</string>
<string name="place_form_error_name_empty">Введите название</string>
<string name="place_form_error_name_too_long">Название слишком длинное</string>
<string name="place_form_error_address_empty">Введите адрес</string>
<string name="place_form_error_address_too_long">Адрес слишком длинный</string>
<string name="place_form_error_latitude_invalid">Некорректная широта (-90 до 90)</string>
<string name="place_form_error_longitude_invalid">Некорректная долгота (-180 до 180)</string>
<string name="place_form_error_coordinates_partial">Введите обе координаты или оставьте оба поля пустыми</string>

<!-- PlaceCategory display names -->
<string name="category_restaurant">Ресторан</string>
<string name="category_cafe">Кафе</string>
<string name="category_park">Парк</string>
<string name="category_museum">Музей</string>
<string name="category_shop">Магазин</string>
<string name="category_landmark">Достопримечательность</string>
<string name="category_other">Другое</string>
```

## Критерии приёма (Definition of Done)

- [ ] Экран работает в режиме создания (FAB → AddEditPlaceScreen без аргументов)
- [ ] Экран работает в режиме редактирования (форма предзаполнена данными существующего места)
- [ ] Заголовок `TopAppBar` различается для создания и редактирования
- [ ] Все поля формы отображаются корректно
- [ ] Выбор категории работает через `ExposedDropdownMenuBox`
- [ ] Валидация срабатывает при нажатии «Сохранить» (поля `name`, `address` — обязательные)
- [ ] Валидация координат: корректный диапазон, согласованность пары широта/долгота
- [ ] Кнопка «Сохранить» недоступна при `isLoading = true`
- [ ] После сохранения — возврат на предыдущий экран, список обновляется реактивно
- [ ] `AddEditPlaceViewModel` не импортирует классы из `data/`
- [ ] `AddEditPlaceViewModel` получает `placeId` через `SavedStateHandle`
- [ ] Unit-тесты для `AddEditPlaceViewModel`:
  - Проверка предзаполнения формы в edit-режиме
  - Проверка валидации (пустое имя, некорректные координаты, частичные координаты)
  - Проверка вызова `SavePlaceUseCase` при корректных данных
  - Проверка эмиссии `NavigateBack` после сохранения
- [ ] Есть `@Preview` для `AddEditPlaceScreen` (режим создания и режим редактирования)
- [ ] Все строки в `strings.xml`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — добавление нового места:**
   - Нажать FAB на `PlacesListScreen`.
   - Заполнить все обязательные поля: название, адрес, выбрать категорию.
   - Нажать «Сохранить».
   - Ожидаемый результат: возврат на список, новое место присутствует в списке.

2. **Счастливый путь — редактирование места:**
   - Открыть детали места → нажать «Редактировать».
   - Изменить название.
   - Нажать «Сохранить».
   - Ожидаемый результат: возврат, обновлённое название отображается в списке и деталях.

3. **Счастливый путь — добавление с координатами:**
   - Ввести корректные широту (55.7558) и долготу (37.6176).
   - Нажать «Сохранить».
   - Ожидаемый результат: место сохранено с координатами, без ошибок.

4. **Граничный случай — пустое обязательное поле:**
   - Оставить поле «Название» пустым, нажать «Сохранить».
   - Ожидаемый результат: отображается ошибка «Введите название», сохранение не происходит.

5. **Граничный случай — некорректная широта:**
   - Ввести `latitude = 95` (выходит за диапазон -90..90).
   - Нажать «Сохранить».
   - Ожидаемый результат: ошибка «Некорректная широта (-90 до 90)».

6. **Граничный случай — только широта без долготы:**
   - Ввести широту, оставить долготу пустой.
   - Нажать «Сохранить».
   - Ожидаемый результат: ошибка «Введите обе координаты или оставьте оба поля пустыми».

7. **Граничный случай — кнопка «Назад» без сохранения:**
   - Заполнить форму, нажать «Назад» без сохранения.
   - Ожидаемый результат: возврат на предыдущий экран, данные не сохранены.
   - Примечание: диалог «несохранённые изменения» — вне scope Sprint 1. Подтверждено Product Owner 2026-03-14.

---

## Статус выполнения

**Статус:** todo

**Ветка:** feature/TASK-008-add-edit-place (заполняет Developer)
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
