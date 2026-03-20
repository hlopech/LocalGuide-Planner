# TASK-013: AddEditPlaceScreen — LocalGuideTextField, AnimatedContent для кнопки, staggered анимация

**Автор:** Tech Director
**Дата создания:** 2026-03-20
**Тип:** refactor
**Приоритет:** High
**Спринт:** SPRINT-2
**Зависимости:** TASK-009, TASK-011

---

## Описание

Текущий `AddEditPlaceScreen` — `Scaffold` со статичным `TopAppBar`, `Column` с `OutlinedTextField`-ами без единого стиля и `ExposedDropdownMenuBox` для выбора категории. Нет анимаций, нет визуальной согласованности с остальными экранами Sprint 2.

Необходимо переработать визуальный слой `AddEditPlaceScreen`: создать переиспользуемый компонент `LocalGuideTextField`, заменить `CategoryDropdown` на горизонтальный `CategorySelector` с `FilterChip`-ами, добавить staggered-анимацию появления полей, оформить кнопку «Сохранить» с `AnimatedContent` для состояния загрузки. ViewModel, UiState, UiEffect — **не трогать**.

### Целевой дизайн

**Общая структура:**
- `Scaffold` с `TopAppBar(scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior())`, `nestedScroll`
- `TopAppBar`: title = «Добавить место» (`R.string.add_edit_place_title_add`) или «Редактировать место» (`R.string.add_edit_place_title_edit`) в зависимости от `uiState.isEditMode`, `navigationIcon = Icons.Rounded.ArrowBack`
- Тело формы: `LazyColumn` (не `Column` + `verticalScroll`) с `contentPadding = PaddingValues(16.dp)`

**LocalGuideTextField (новый reusable composable):**

Создать в `ui/places/common/LocalGuideTextField.kt`:
```kotlin
@Composable
fun LocalGuideTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
)
```
- `OutlinedTextField` с `shape = MaterialTheme.shapes.medium` (16.dp из TASK-009)
- Кастомные `colors`: `focusedBorderColor = MaterialTheme.colorScheme.primary`, `focusedLabelColor = MaterialTheme.colorScheme.primary`
- `leadingIcon = { Icon(leadingIcon, contentDescription = null) }` — показывать только если параметр не null
- `supportingText = { Text(errorMessage) }` — показывать только если `isError = true` и `errorMessage != null`
- `modifier = modifier.fillMaxWidth()`

**Поля формы с staggered анимацией:**

Каждое поле появляется с `AnimatedVisibility` + stagger (`delayMillis = minOf(index * 80L, 400L).toInt()`):

| Индекс | Поле | leadingIcon | Параметры |
|--------|------|-------------|-----------|
| 0 | Название | `Icons.Rounded.Label` | `isError = uiState.nameError != null`, `errorMessage = uiState.nameError` |
| 1 | Адрес | `Icons.Rounded.LocationOn` | — |
| 2 | Описание | `Icons.Rounded.Description` | `maxLines = 5` |
| 3 | CategorySelector | — | отдельный composable |
| 4 | FavoriteToggle | — | `Row` с `Switch` и `Text` |

`FavoriteToggle` — `Row(verticalAlignment = CenterVertically, modifier = fillMaxWidth())`:
- `Text(R.string.add_edit_place_label_favorite, modifier = Modifier.weight(1f))`
- `Switch(checked = uiState.isFavorite, onCheckedChange = onFavoriteChange)` (если поле есть в UiState; если нет — опустить)

**CategorySelector:**
```kotlin
@Composable
private fun CategorySelector(
    selectedCategory: PlaceCategory,
    onCategorySelected: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier,
)
```
Метка над селектором: `Text(R.string.add_edit_place_label_category, style = labelLarge, color = onSurfaceVariant)`.

`LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp))` с `FilterChip` для каждой `PlaceCategory.entries`:
```kotlin
FilterChip(
    selected = category == selectedCategory,
    onClick = { onCategorySelected(category) },
    label = { Text(stringResource(category.toStringRes())) },
    leadingIcon = {
        Icon(
            imageVector = category.toIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    },
)
```

**AnimatedContent для кнопки сохранения:**
```kotlin
Button(
    onClick = onSaveClicked,
    enabled = !uiState.isSaving,
    shape = MaterialTheme.shapes.extraLarge,
    modifier = Modifier.fillMaxWidth(),
) {
    AnimatedContent(
        targetState = uiState.isSaving,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
    ) { isSaving ->
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text = stringResource(R.string.add_edit_place_button_save))
        }
    }
}
```

---

## Предусловия

- [ ] TASK-009 выполнена: `LocalGuidePlannerTheme`, `MaterialTheme.shapes.medium` (16.dp), `MaterialTheme.shapes.extraLarge` (50.dp) доступны
- [ ] TASK-011 выполнена: `PlaceCategoryExt.kt` содержит `toIcon(): ImageVector` и `toStringRes(): Int` для всех 7 категорий

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|--------------------|
| `ui/places/common/LocalGuideTextField.kt` | create | Новый reusable composable с `OutlinedTextField`, кастомными цветами и `shape=shapes.medium` |
| `ui/places/addedit/AddEditPlaceScreen.kt` | modify | Переработать `AddEditPlaceContent` и дочерние composable; заменить `CategoryDropdown` на `CategorySelector`, поля на `LocalGuideTextField`, `Column` + `verticalScroll` на `LazyColumn` |

### Архитектурные решения

- `AddEditPlaceViewModel`, `AddEditPlaceUiState`, `AddEditPlaceUiEffect` — **не трогать**. Только визуальный слой.
- `AddEditPlaceScreen` (функция со слоем ViewModel) остаётся без изменений.
- `LocalGuideTextField` — stateless composable в `ui/places/common/`. Не содержит бизнес-логику, не обращается к ViewModel.
- Анимационные состояния (`visible: Boolean`) — локальные переменные `remember { mutableStateOf(false) }` в родительском composable формы, запускаются через `LaunchedEffect(Unit)`.
- `LazyColumn` заменяет `Column + verticalScroll` для корректного `nestedScroll` с `TopAppBar`.
- `scrollBehavior` (`TopAppBarScrollBehavior`) — локальная переменная в `AddEditPlaceContent`, передаётся в `Scaffold` и `TopAppBar`.
- `CategorySelector` — private composable в `AddEditPlaceScreen.kt`, использует `toIcon()` и `toStringRes()` из `PlaceCategoryExt`.
- `AnimatedContent` требует `@OptIn(ExperimentalAnimationApi::class)` — проверить актуальность аннотации для используемой версии Compose.
- Если `AddEditPlaceUiState` не содержит поля `isFavorite` — `FavoriteToggle` не добавлять (не менять UiState).

### Структура composable

```
AddEditPlaceContent(uiState, callbacks)
├── TopAppBar(scrollBehavior=enterAlways, title=add/edit, nav=ArrowBack)
└── LazyColumn
    ├── AnimatedFormField(index=0) → LocalGuideTextField(name, Icons.Rounded.Label)
    ├── AnimatedFormField(index=1) → LocalGuideTextField(address, Icons.Rounded.LocationOn)
    ├── AnimatedFormField(index=2) → LocalGuideTextField(description, Icons.Rounded.Description, maxLines=5)
    ├── AnimatedFormField(index=3) → CategorySelector(selectedCategory, onCategorySelected)
    └── AnimatedFormField(index=4) → SaveButton(isSaving, onSaveClicked)

LocalGuideTextField(value, onValueChange, label, leadingIcon?, isError, errorMessage?, maxLines, keyboardOptions)
CategorySelector(selectedCategory, onCategorySelected) — LazyRow с FilterChip
SaveButton(isSaving, onClick) — Button + AnimatedContent

AnimatedFormField(index, content)  // private wrapper с LaunchedEffect + AnimatedVisibility
```

Каждая функция не длиннее 50 строк.

### Необходимые зависимости (build.gradle.kts)

`material-icons-extended` — должна быть подключена (см. TASK-010/TASK-011). Иконки `Icons.Rounded.Label`, `Icons.Rounded.LocationOn`, `Icons.Rounded.Description` входят в этот артефакт.

### Строки (strings.xml — добавить)

```xml
<string name="add_edit_place_title_add">Добавить место</string>
<string name="add_edit_place_title_edit">Редактировать место</string>
<string name="add_edit_place_button_save">Сохранить</string>
<string name="add_edit_place_label_name">Название</string>
<string name="add_edit_place_label_address">Адрес</string>
<string name="add_edit_place_label_description">Описание (необязательно)</string>
<string name="add_edit_place_label_category">Категория</string>
<string name="add_edit_place_label_favorite">В избранное</string>
```

Проверить существующие строки: `add_edit_place_title_add`, `add_edit_place_title_edit`, `add_edit_place_action_save` могут уже присутствовать в `strings.xml` — не дублировать, использовать существующие ключи. Ключ `add_edit_place_action_save` заменить на `add_edit_place_button_save` если в коде не используется; или добавить новый, оставив старый.

## Критерии приёма (Definition of Done)

- [ ] `LocalGuideTextField.kt` создан в `ui/places/common/`, содержит `@Composable fun LocalGuideTextField(...)` согласно сигнатуре выше
- [ ] `LocalGuideTextField` переиспользуется для полей «Название», «Адрес», «Описание» в `AddEditPlaceScreen`
- [ ] Поле «Название» показывает ошибку при `uiState.nameError != null`
- [ ] Все поля формы используют `shape = MaterialTheme.shapes.medium` и `focusedBorderColor = primary`
- [ ] Поля формы появляются с staggered-анимацией (`delayMillis = index * 80`, не более 400ms)
- [ ] `CategorySelector` — `LazyRow` с `FilterChip` для всех 7 категорий (`PlaceCategory.entries`)
- [ ] Выбранная категория визуально выделена (`selected = true` у соответствующего `FilterChip`)
- [ ] Каждый `FilterChip` содержит иконку категории через `category.toIcon()`
- [ ] Кнопка «Сохранить» имеет `shape = MaterialTheme.shapes.extraLarge` (pill-форма) и `fillMaxWidth()`
- [ ] При `uiState.isSaving = true` внутри кнопки отображается `CircularProgressIndicator` через `AnimatedContent`
- [ ] `TopAppBar` скрывается при скролле вниз (`enterAlwaysScrollBehavior`)
- [ ] Нет hardcoded цветов (все через `MaterialTheme`)
- [ ] Нет hardcoded строк (все через `stringResource`)
- [ ] Функции не длиннее 50 строк
- [ ] `@Preview` для add-режима, edit-режима и isSaving=true обновлены и рендерятся
- [ ] `AddEditPlaceViewModel`, `AddEditPlaceUiState`, `AddEditPlaceUiEffect` не изменены
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Staggered-анимация формы:**
   - Открыть экран добавления места.
   - Ожидаемый результат: поля «Название», «Адрес», «Описание», селектор категории и кнопка «Сохранить» появляются последовательно с задержкой.

2. **Выбор категории:**
   - На экране добавления прокрутить горизонтальный список категорий.
   - Нажать на категорию MUSEUM.
   - Ожидаемый результат: `FilterChip` MUSEUM становится `selected`, остальные — нет. Видна иконка музея.

3. **Состояние сохранения:**
   - Заполнить форму, нажать «Сохранить».
   - Ожидаемый результат: текст «Сохранить» плавно сменяется `CircularProgressIndicator` через `AnimatedContent`. Кнопка недоступна (`enabled = false`).

4. **Режим редактирования:**
   - Открыть форму редактирования существующего места.
   - Ожидаемый результат: заголовок TopAppBar — «Редактировать место», поля заполнены данными места, выбранная категория подсвечена.

5. **Валидация имени:**
   - Оставить поле «Название» пустым, нажать «Сохранить».
   - Ожидаемый результат: под полем «Название» появляется текст ошибки, граница поля красная (`isError = true`).

6. **Скрытие TopAppBar:**
   - На форме с длинным содержимым скролл вниз.
   - Ожидаемый результат: `TopAppBar` плавно скрывается. Скролл вверх — появляется.

7. **Фокус на поле:**
   - Тапнуть по полю «Адрес».
   - Ожидаемый результат: граница поля и label меняют цвет на `primary`.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-013-add-edit-place-ui
**PR:** https://github.com/hlopech/LocalGuide-Planner/pull/14
**QA вердикт:** pending
**Мерж:** (заполняет Orchestrator)

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-20 | todo | Tech Director | Задача создана |
| 2026-03-20 | review | Developer | Реализован LocalGuideTextField, CategorySelector с FilterChip, AnimatedContent на кнопке, staggered-анимация полей |
