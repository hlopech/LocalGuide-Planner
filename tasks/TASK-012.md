# TASK-012: PlaceDetailScreen — hero-секция с градиентом категории, LargeTopAppBar, анимированные секции

**Автор:** Tech Director
**Дата создания:** 2026-03-20
**Тип:** refactor
**Приоритет:** High
**Спринт:** SPRINT-2
**Зависимости:** TASK-009, TASK-011

---

## Описание

Текущий `PlaceDetailScreen` — `Scaffold` со статичным `TopAppBar`, `Column` со списком `Text`-полей и FAB для редактирования. Нет визуальной иерархии, нет анимаций, акцент категории не используется.

Необходимо переработать визуальный слой `PlaceDetailScreen`: добавить hero-секцию с градиентом цвета категории, заменить статичный TopAppBar на `LargeTopAppBar` со сворачивающимся поведением, оформить секции информации как карточки с анимированным появлением. ViewModel, UiState, UiEffect — **не трогать**.

### Целевой дизайн

**Hero-секция (верхняя часть под TopAppBar):**
- `Box` с `height(200.dp)`, `fillMaxWidth`
- Фон: `Brush.linearGradient` от `category.toAccentColor().copy(alpha=0.7f)` до `category.toAccentColor()`
- Скругление нижних углов: `RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)`
- По центру: `Icon(category.toIcon(), modifier=Modifier.size(72.dp), tint=Color.White)` с `AnimatedVisibility(fadeIn + scaleIn)`
- Под иконкой: `Text(place.name, headlineMedium, Color.White, textAlign=Center)`
- Если `isFavorite`: `Icon(Icons.Rounded.Favorite, tint=Color(0xFFE53935), modifier=Modifier.size(20.dp))` справа от названия в `Row`

**LargeTopAppBar с exitUntilCollapsedScrollBehavior:**
```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        LargeTopAppBar(
            title = { Text(place.name) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onNavigateToEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    },
    floatingActionButton = {
        FloatingActionButton(onClick = onNavigateToEdit) {
            Icon(Icons.Rounded.Edit, contentDescription = null)
        }
    }
)
```

**Секции информации (анимированные):**

Каждая секция появляется с `AnimatedVisibility` + stagger (аналогично TASK-011, `delayMillis = index * 80`, максимум 400ms):

`DetailSection` — `Card(shape=MaterialTheme.shapes.medium)` с `Column(padding=16.dp)`:
- `Row`: `Icon(size=24.dp, tint=MaterialTheme.colorScheme.primary)` + `Text(label, labelLarge)` + `Text(value, bodyLarge)`

Отображаемые секции (по порядку):
1. Адрес — `Icons.Rounded.LocationOn`, строка `R.string.place_detail_label_address`, значение `place.address`
2. Категория — `category.toIcon()`, строка `R.string.place_detail_label_category`, значение `stringResource(category.toStringRes())`
3. Описание — `Icons.Rounded.Description`, строка `R.string.place_detail_label_description`, значение `place.description` — показывать **только если не пусто**
4. Дата добавления — `Icons.Rounded.CalendarToday`, строка `R.string.place_detail_label_added`, значение форматированной даты из `place.createdAt`

Кнопка переключения избранного — `OutlinedButton(modifier=fillMaxWidth())`:
- Иконка: `Icons.Rounded.FavoriteBorder` / `Icons.Rounded.Favorite`
- Текст: `R.string.place_detail_action_add_favorite` / `R.string.place_detail_action_remove_favorite`
- Появляется последней в stagger-последовательности

**Переключение состояний:**

Использовать `Crossfade(targetState = uiState)` для переключения между тремя состояниями:
- **Loading:** `CircularProgressIndicator` по центру экрана
- **Error:** `Column` с `Icon(Icons.Rounded.ErrorOutline, size=48.dp, tint=error)` + `Text(message, bodyLarge, error)` + `Button` с текстом `R.string.place_detail_error_retry` (вызывает перезагрузку через ViewModel) + кнопка «Назад»
- **Success:** hero-секция + секции информации

---

## Предусловия

- [ ] TASK-009 выполнена: `LocalGuidePlannerTheme`, `LocalGuideDesignTokens`, Shapes, Typography доступны
- [ ] TASK-011 выполнена: `PlaceCategoryExt.kt` содержит `toIcon(): ImageVector` и `toAccentColor(): Color` для всех 7 категорий

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|--------------------|
| `ui/places/detail/PlaceDetailScreen.kt` | modify | Переработать `PlaceDetailContent` и дочерние composable согласно дизайну |

### Архитектурные решения

- `PlaceDetailViewModel`, `PlaceDetailUiState`, `PlaceDetailUiEffect` — **не трогать**. Только визуальный слой.
- `PlaceDetailScreen` (функция со слоем ViewModel) остаётся без изменений.
- Анимационные состояния (`visible: Boolean` для stagger) — локальные переменные `remember { mutableStateOf(false) }` в composable секций. В ViewModel не попадают.
- `scrollBehavior` (`TopAppBarScrollBehavior`) — локальная переменная в `PlaceDetailContent`, передаётся в `Scaffold` и `LargeTopAppBar`.
- `Crossfade` — для переключения между Loading / Error / Success.
- `AnimatedVisibility` с нарастающим `delayMillis` — для stagger-анимации секций.
- `LargeTopAppBar` требует `@OptIn(ExperimentalMaterial3Api::class)`.
- Цвет `FavoriteRed` = `Color(0xFFE53935)` — выделить в локальную константу внутри файла, не хардкодить inline.

### Структура composable

```
PlaceDetailContent(uiState, callbacks)
├── LargeTopAppBar (scrollBehavior)
└── Crossfade(uiState)
    ├── Loading → PlaceDetailLoadingContent()
    ├── Error   → PlaceDetailErrorContent(message, onRetry, onNavigateBack)
    └── Success → PlaceDetailSuccessContent(place, onToggleFavorite, onEditClicked)
                  ├── PlaceDetailHeroSection(place, visible)
                  │   ├── Box(Brush.linearGradient, RoundedCornerShape)
                  │   ├── AnimatedVisibility → Icon(category.toIcon(), 72.dp)
                  │   └── AnimatedVisibility → Row(Text(name) + FavoriteIcon?)
                  └── PlaceDetailSectionsColumn(place, onToggleFavorite)
                      ├── AnimatedDetailSection(index=0, icon, label, value)  // Адрес
                      ├── AnimatedDetailSection(index=1, icon, label, value)  // Категория
                      ├── AnimatedDetailSection(index=2, ...) если description не пусто
                      ├── AnimatedDetailSection(index=..., icon, label, value) // Дата
                      └── AnimatedFavoriteButton(index=..., isFavorite, onClick)

DetailSection(icon, label, value, modifier)   // private, stateless Card-обёртка
AnimatedDetailSection(index, ...)             // private, добавляет stagger AnimatedVisibility
```

Каждая функция не длиннее 50 строк.

### Необходимые зависимости (build.gradle.kts)

`material-icons-extended` — должна быть подключена (см. TASK-010/TASK-011). Иконки `Icons.Rounded.ErrorOutline`, `Icons.Rounded.CalendarToday`, `Icons.Rounded.Description` входят в этот артефакт.

### Строки (strings.xml — добавить)

```xml
<string name="place_detail_label_address">Адрес</string>
<string name="place_detail_label_category">Категория</string>
<string name="place_detail_label_description">Описание</string>
<string name="place_detail_label_added">Дата добавления</string>
<string name="place_detail_action_add_favorite">В избранное</string>
<string name="place_detail_action_remove_favorite">Убрать из избранного</string>
<string name="place_detail_error_retry">Повторить</string>
```

Существующие строки (`place_detail_edit`, `place_detail_delete`, `place_detail_not_found`, диалог удаления) — оставить без изменений.

## Критерии приёма (Definition of Done)

- [ ] Hero-секция отображает градиент цвета категории, иконку категории (72.dp) и название места
- [ ] При `isFavorite = true` рядом с названием отображается иконка сердца
- [ ] `LargeTopAppBar` сворачивается при скролле вниз (`exitUntilCollapsedScrollBehavior`)
- [ ] Секции информации (адрес, категория, описание, дата) отображаются как `Card` с иконкой и текстом
- [ ] Секции появляются с staggered-анимацией (`delayMillis = index * 80`)
- [ ] Секция «Описание» скрыта если `place.description` пустая строка
- [ ] Кнопка «В избранное» / «Убрать из избранного» переключает состояние через ViewModel
- [ ] Три состояния (Loading / Error / Success) переключаются через `Crossfade`
- [ ] Error state отображает иконку ошибки, текст и кнопки «Повторить» и «Назад»
- [ ] Нет hardcoded цветов вне токенов и локальных констант
- [ ] Нет hardcoded строк (все через `stringResource`)
- [ ] Функции не длиннее 50 строк
- [ ] `@Preview` для всех трёх состояний (Loading, Error, Success) обновлены и рендерятся
- [ ] `PlaceDetailViewModel`, `PlaceDetailUiState`, `PlaceDetailUiEffect` не изменены
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Hero-секция и цвет категории:**
   - Открыть карточку места категории PARK, затем RESTAURANT.
   - Ожидаемый результат: hero-секция показывает разные цвета градиента для каждой категории, соответствующие иконки.

2. **Сворачивание TopAppBar:**
   - На экране детали скролл вниз.
   - Ожидаемый результат: `LargeTopAppBar` плавно сворачивается (большой заголовок исчезает, остаётся компактный). Скролл вверх — разворачивается обратно.

3. **Избранное:**
   - Открыть место, не находящееся в избранном. Нажать «В избранное».
   - Ожидаемый результат: кнопка переключается на «Убрать из избранного», иконка сердца появляется в hero-секции.
   - Нажать «Убрать из избранного».
   - Ожидаемый результат: кнопка возвращается к «В избранное», иконка сердца исчезает.

4. **Staggered-анимация секций:**
   - Открыть экран детали места.
   - Ожидаемый результат: секции информации появляются одна за другой с небольшой задержкой.

5. **Loading state:**
   - Смоделировать состояние загрузки (через Preview или задержку).
   - Ожидаемый результат: отображается `CircularProgressIndicator` по центру экрана.

6. **Error state:**
   - Смоделировать состояние ошибки (через Preview или несуществующий ID).
   - Ожидаемый результат: иконка ошибки, текст сообщения, кнопка «Повторить» и кнопка «Назад».

7. **Описание пустое:**
   - Открыть место без описания.
   - Ожидаемый результат: секция «Описание» не отображается.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-012-place-detail-ui
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** (заполняет Orchestrator)

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-20 | todo | Tech Director | Задача создана |
| 2026-03-20 | review | Developer | Реализован PlaceDetailScreen с hero-секцией, LargeTopAppBar, анимированными секциями |
