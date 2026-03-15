# TASK-008: AddEditPlaceScreen — универсальная форма добавления и редактирования места

**Автор:** Tech Director
**Дата создания:** 2026-03-15
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-002, TASK-003, TASK-005, TASK-006, TASK-007

---

## Описание

Реализовать универсальный экран формы `AddEditPlaceScreen` с двумя режимами работы:

- **Режим добавления** (`placeId = null`): открывается по нажатию FAB в `PlacesListScreen` через маршрут `Screen.AddPlace` (`places/add`). Форма пустая, заголовок TopAppBar — «Добавить место».
- **Режим редактирования** (`placeId != null`): открывается из `PlaceDetailScreen` через `onNavigateToEdit(placeId)` по маршруту `Screen.EditPlace(placeId)` (`places/edit/{placeId}`). Поля формы предзаполнены данными из Room. Заголовок TopAppBar — «Редактировать место».

Режим определяется наличием `placeId` в `SavedStateHandle`. Для `Screen.AddPlace` аргумент навигации не передаётся — ViewModel читает `null`. Для `Screen.EditPlace` аргумент `placeId` передаётся как обязательный `NavType.StringType`.

Оба маршрута (`AddPlace` и `EditPlace`) должны использовать один и тот же Composable `AddEditPlaceScreen` — это единая точка входа для обоих сценариев. AppNavGraph обновляется заменой `Text`-заглушек на реальный экран.

После сохранения или нажатия кнопки «Назад» — экран закрывается через `navController.popBackStack()`.

## Предусловия

- [ ] TASK-002 выполнена: `GetPlaceByIdUseCase`, `SavePlaceUseCase` существуют и работают
- [ ] TASK-003 выполнена: `PlacesRepositoryImpl` работает с Room
- [ ] TASK-005 выполнена: `Screen.AddPlace`, `Screen.EditPlace` определены в `Screen.kt`, маршруты зарегистрированы в `AppNavGraph.kt` (сейчас — заглушки `Text`)
- [ ] TASK-006 выполнена: FAB в `PlacesListScreen` вызывает `onNavigateToAdd()` — уже реализовано
- [ ] TASK-007 выполнена: кнопка редактирования в `PlaceDetailScreen` вызывает `onNavigateToEdit(placeId)` — уже реализовано

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/places/addedit/AddEditPlaceUiState.kt` | create | Data class состояния экрана формы |
| `ui/places/addedit/AddEditPlaceViewModel.kt` | create | HiltViewModel с SavedStateHandle, логика обоих режимов |
| `ui/places/addedit/AddEditPlaceScreen.kt` | create | Compose-экран с формой добавления/редактирования |
| `ui/navigation/AppNavGraph.kt` | modify | Заменить Text-заглушки для `AddPlace` и `EditPlace` на `AddEditPlaceScreen` |

### Архитектурные решения

- `AddEditPlaceViewModel` аннотируется `@HiltViewModel` и получает `SavedStateHandle` через конструктор. Из `SavedStateHandle` читается `placeId: String?` по ключу `Screen.EditPlace.ARG_PLACE_ID`. Значение `null` означает режим добавления; непустая строка — режим редактирования.
- В `init`-блоке: если `placeId != null` — вызвать `getPlaceByIdUseCase(placeId)` и заполнить поля формы в `UiState`. Если `placeId == null` — оставить поля пустыми, `isEditMode = false`.
- Сохранение через `SavePlaceUseCase`: в режиме добавления создаётся `Place` с `id = ""` — `SavePlaceUseCase` внутри присваивает UUID автоматически (см. существующую реализацию). В режиме редактирования — `Place` с оригинальным `id`, `createdAt`, `isFavorite`, `latitude`, `longitude` из загруженных данных, чтобы не затереть эти поля.
- Навигационные эффекты (`NavigateBack`) передаются через `SharedFlow<AddEditPlaceUiEffect>`, не через флаг в `UiState`.
- Валидация выполняется в ViewModel перед вызовом UseCase: поле `name` не может быть пустым. Ошибки отображаются как `isError` + `supportingText` у конкретного `OutlinedTextField`.
- ViewModel не импортирует ни один класс из пакета `data/`.
- Экран не хранит локальное состояние Compose — все данные формы и ошибки живут в `UiState` в ViewModel.

### UiState

```kotlin
// ui/places/addedit/AddEditPlaceUiState.kt
data class AddEditPlaceUiState(
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val category: PlaceCategory = PlaceCategory.OTHER,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
)
```

Поле `nameError` содержит строку с текстом ошибки (берётся из `strings.xml` на уровне экрана) или `null` если поле валидно. Дополнительные поля валидации добавлять только при реальной необходимости.

### UiEffect

```kotlin
// в том же файле AddEditPlaceUiState.kt или отдельном
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
    private val savePlaceUseCase: SavePlaceUseCase,
) : ViewModel() {

    private val placeId: String? = savedStateHandle[Screen.EditPlace.ARG_PLACE_ID]

    private val _uiState = MutableStateFlow(AddEditPlaceUiState())
    val uiState: StateFlow<AddEditPlaceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AddEditPlaceUiEffect>()
    val uiEffect: SharedFlow<AddEditPlaceUiEffect> = _uiEffect.asSharedFlow()

    init {
        if (placeId != null) {
            loadPlace(placeId)
        }
    }

    private fun loadPlace(id: String) { /* загрузить место, заполнить _uiState */ }

    fun onNameChange(value: String) { /* обновить name, сбросить nameError */ }
    fun onAddressChange(value: String) { /* обновить address */ }
    fun onDescriptionChange(value: String) { /* обновить description */ }
    fun onCategoryChange(value: PlaceCategory) { /* обновить category */ }

    fun onSaveClicked() { /* валидация → savePlaceUseCase → emit NavigateBack */ }
    fun onNavigateBackClicked() { /* emit NavigateBack */ }
}
```

Метод `loadPlace` устанавливает `isLoading = true` до вызова UseCase и `isLoading = false` после. В `isEditMode = true` при успешной загрузке. Если `getPlaceByIdUseCase` вернул `null` — эмитировать `NavigateBack` (место не существует, нечего редактировать).

### Экран (ключевые требования)

- Функция `AddEditPlaceScreen` принимает лямбду `onNavigateBack: () -> Unit` и `modifier: Modifier = Modifier`, ViewModel подключается через `hiltViewModel()`.
- `LaunchedEffect` подписывается на `uiEffect` и при `NavigateBack` вызывает `onNavigateBack()`.
- Корневой контейнер — `Scaffold` с `TopAppBar`:
  - Кнопка «Назад» (`Icons.AutoMirrored.Filled.ArrowBack`) вызывает `viewModel.onNavigateBackClicked()`.
  - Заголовок: `stringResource(R.string.add_edit_place_title_add)` если `!isEditMode`, иначе `stringResource(R.string.add_edit_place_title_edit)`.
- Тело экрана — вертикально прокручиваемый `Column` (через `Modifier.verticalScroll`) с отступами `horizontal = 16.dp, vertical = 16.dp`:
  - `OutlinedTextField` для `name` (обязательное поле): `isError = uiState.nameError != null`, `supportingText` — текст ошибки из `uiState.nameError`.
  - `OutlinedTextField` для `address`.
  - `OutlinedTextField` для `description` (`minLines = 3`).
  - `ExposedDropdownMenuBox` с `ExposedDropdownMenu` для выбора категории (`PlaceCategory`). Отображает значения через строки ресурсов (переиспользовать маппинг `PlaceCategory.toStringRes()` из `PlaceDetailScreen`).
  - `Button` «Сохранить» (занимает всю ширину, `Modifier.fillMaxWidth()`), задизейблен если `isSaving = true`.
- При `isLoading = true` — `CircularProgressIndicator` по центру поверх формы (внутри `Box`).

### Обновление AppNavGraph

Заменить два Text-заглушки в `AppNavGraph.kt`:

```kotlin
// Screen.AddPlace — режим добавления
composable(route = Screen.AddPlace.route) {
    AddEditPlaceScreen(
        onNavigateBack = { navController.popBackStack() },
    )
}

// Screen.EditPlace — режим редактирования
composable(
    route = Screen.EditPlace.ROUTE,
    arguments = listOf(
        navArgument(Screen.EditPlace.ARG_PLACE_ID) { type = NavType.StringType },
    ),
) {
    AddEditPlaceScreen(
        onNavigateBack = { navController.popBackStack() },
    )
}
```

`placeId` ViewModel получает автоматически через `SavedStateHandle` — NavController прокидывает аргументы навигации в неё при использовании Hilt + Navigation Compose.

### Строки для strings.xml (добавить)

```xml
<string name="add_edit_place_title_add">Добавить место</string>
<string name="add_edit_place_title_edit">Редактировать место</string>
<string name="add_edit_place_field_name">Название</string>
<string name="add_edit_place_field_address">Адрес</string>
<string name="add_edit_place_field_description">Описание</string>
<string name="add_edit_place_field_category">Категория</string>
<string name="add_edit_place_action_save">Сохранить</string>
<string name="add_edit_place_error_name_empty">Введите название места</string>
```

## Критерии приёма (Definition of Done)

- [ ] `AddEditPlaceScreen` отображается при нажатии FAB в `PlacesListScreen` (режим добавления, пустая форма)
- [ ] `AddEditPlaceScreen` отображается при нажатии кнопки «Редактировать» в `PlaceDetailScreen` с предзаполненными данными места
- [ ] Заголовок `TopAppBar` корректно меняется в зависимости от режима
- [ ] При сохранении с пустым полем `name` — отображается текст ошибки под полем, UseCase не вызывается
- [ ] Успешное сохранение нового места добавляет запись в Room и возвращает на предыдущий экран
- [ ] Успешное сохранение в режиме редактирования обновляет существующую запись в Room и возвращает на предыдущий экран
- [ ] При редактировании поля `id`, `createdAt`, `isFavorite`, `latitude`, `longitude` оригинального места не изменяются
- [ ] Кнопка «Назад» в `TopAppBar` возвращает на предыдущий экран без сохранения
- [ ] `AddEditPlaceViewModel` получает `placeId` через `SavedStateHandle`
- [ ] `AddEditPlaceViewModel` не импортирует ни один класс из пакета `data/`
- [ ] Навигационные события — через `SharedFlow<AddEditPlaceUiEffect>`, не через флаг в `UiState`
- [ ] `AppNavGraph.kt` обновлён: заглушки `Text` заменены на `AddEditPlaceScreen`
- [ ] Unit-тесты для `AddEditPlaceViewModel` написаны и зелёные (минимум: сценарии из раздела «Тест-сценарии»)
- [ ] Есть `@Preview` для `AddEditPlaceScreen` в режиме добавления (пустая форма)
- [ ] Есть `@Preview` для `AddEditPlaceScreen` в режиме редактирования (заполненная форма)
- [ ] Все строки через `strings.xml`, нет hardcoded значений
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — добавление нового места:**
   - Открыть список мест, нажать FAB.
   - Ожидаемый результат: открывается форма с заголовком «Добавить место», все поля пустые.
   - Заполнить все поля, нажать «Сохранить».
   - Ожидаемый результат: экран закрывается, новое место появляется в `PlacesListScreen` реактивно (без перезагрузки).

2. **Счастливый путь — редактирование существующего места:**
   - Открыть детали любого места, нажать иконку редактирования.
   - Ожидаемый результат: открывается форма с заголовком «Редактировать место», поля предзаполнены данными места.
   - Изменить название и адрес, нажать «Сохранить».
   - Ожидаемый результат: экран закрывается, `PlaceDetailScreen` при повторном открытии отображает обновлённые данные.

3. **Счастливый путь — категория сохраняется корректно:**
   - В режиме добавления выбрать категорию, отличную от `OTHER`, сохранить место.
   - Открыть детали созданного места.
   - Ожидаемый результат: отображается выбранная категория.

4. **Граничный случай — сохранение с пустым именем:**
   - Открыть форму добавления, оставить поле «Название» пустым, нажать «Сохранить».
   - Ожидаемый результат: под полем «Название» появляется сообщение об ошибке «Введите название места», переход на другой экран не происходит.

5. **Граничный случай — кнопка «Назад» без сохранения:**
   - Открыть форму добавления, частично заполнить поля, нажать кнопку «Назад» в TopAppBar.
   - Ожидаемый результат: экран закрывается, новое место не создаётся в списке.

6. **Граничный случай — редактирование не перезаписывает isFavorite:**
   - Добавить место в избранное через `PlaceDetailScreen`.
   - Перейти к редактированию этого места, изменить только название, сохранить.
   - Открыть детали места снова.
   - Ожидаемый результат: место по-прежнему в избранном.

7. **Негативный сценарий — режим редактирования для несуществующего ID:**
   - Открыть `AddEditPlaceScreen` с несуществующим `placeId` (программно, через навигацию).
   - Ожидаемый результат: экран немедленно закрывается (ViewModel эмитирует `NavigateBack`), приложение не крашится.

### Unit-тесты для AddEditPlaceViewModel (обязательный минимум)

| # | Тест | Что проверяет |
|---|------|---------------|
| 1 | `onSaveClicked with empty name emits nameError` | Валидация: `nameError != null`, UseCase не вызывается |
| 2 | `onSaveClicked with valid data calls savePlaceUseCase` | Happy path добавления |
| 3 | `onSaveClicked emits NavigateBack after save` | UiEffect после успешного сохранения |
| 4 | `init with placeId loads place and sets isEditMode true` | Загрузка данных в режиме редактирования |
| 5 | `init with null placeId keeps isEditMode false` | Режим добавления — поля пусты |
| 6 | `init with unknown placeId emits NavigateBack` | Несуществующий ID → выход |
| 7 | `onSaveClicked in edit mode preserves original id and createdAt` | Не затирает служебные поля |
| 8 | `onNameChange clears nameError` | Сброс ошибки при вводе |

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-008-add-edit-place
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-15 | todo | Tech Director | Задача создана |
| 2026-03-15 | review | Developer | Реализация завершена, открыт PR |
