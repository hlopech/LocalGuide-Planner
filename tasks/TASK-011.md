# TASK-011: Список мест — PlaceCard с тенями, staggered-анимация, animated TopBar, extended FAB

**Автор:** Tech Director
**Дата создания:** 2026-03-15
**Тип:** refactor
**Приоритет:** High
**Спринт:** SPRINT-2
**Зависимости:** TASK-009

---

## Описание

Текущий `PlacesListScreen` выводит карточки через `PlaceCard` (которой сейчас нет — она вызывается но файл в `common/` отсутствует). FAB — простой `FloatingActionButton`. TopBar — статичный `TopAppBar` из `MainScreen`.

Необходимо:
1. Создать красивый `PlaceCard` с тенью, скруглёнными углами, цветным акцентом категории.
2. Добавить staggered-анимацию появления карточек в списке.
3. Переработать FAB на `ExtendedFloatingActionButton`, который сворачивается при скролле.
4. Добавить в `MainScreen` `LargeTopAppBar` (или `TopAppBar` с `scrollBehavior`), который прячется при скролле.

**Важно:** `PlacesListViewModel`, `PlacesListUiState`, логика — **не трогать**. Только UI.

### PlaceCard — целевой дизайн

Структура карточки (`ElevatedCard` или `Card` с `elevation`):
```
ElevatedCard(
    shape = MaterialTheme.shapes.large,         // 24.dp из TASK-009
    elevation = CardDefaults.elevatedCardElevation(
        defaultElevation = LocalGuideDesignTokens.cardElevation  // 2.dp
    )
)
├── Row (fillMaxWidth, padding 16.dp)
│   ├── CategoryAccentBox (48x48.dp, скруглённые углы 12.dp, цвет категории)
│   │   └── Icon категории (24.dp, белый)
│   └── Column (weight 1f, paddingStart 12.dp)
│       ├── Text(place.name, titleLarge, maxLines=1, overflow=Ellipsis)
│       ├── Text(place.address, bodySmall, onSurfaceVariant, maxLines=1)
│       └── Row (paddingTop 8.dp)
│           ├── CategoryChip(place.category)  // SuggestionChip маленький
│           └── if (place.isFavorite) FavoriteIndicator
└── SwipeToDismiss или IconButton(delete) в trailing position
```

**CategoryAccentBox** — `Box` с `background(color = categoryColor, shape = RoundedCornerShape(12.dp))`. Цвета брать из `LocalGuideDesignTokens` (`categoryColorRestaurant`, `categoryColorCafe`, и т.д.).

**Иконки категорий** (использовать `Icons.Rounded.*`):
| Категория | Иконка |
|-----------|--------|
| RESTAURANT | `Icons.Rounded.Restaurant` |
| CAFE | `Icons.Rounded.LocalCafe` |
| PARK | `Icons.Rounded.Park` |
| MUSEUM | `Icons.Rounded.Museum` |
| SHOP | `Icons.Rounded.ShoppingBag` |
| LANDMARK | `Icons.Rounded.AccountBalance` |
| OTHER | `Icons.Rounded.Place` |

Добавить функцию-расширение `PlaceCategory.toIcon(): ImageVector` в `ui/places/common/PlaceCategoryExt.kt` (рядом с `toStringRes()`).

Добавить функцию-расширение `PlaceCategory.toAccentColor(): Color` в `ui/places/common/PlaceCategoryExt.kt`, возвращающую цвет из `LocalGuideDesignTokens`.

**Кнопка удаления** — `IconButton` справа в `Row`, иконка `Icons.Rounded.Delete`, `tint = MaterialTheme.colorScheme.error`.

**Favourite indicator** — `Icon(Icons.Rounded.Favorite, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))` рядом с Chip.

### Staggered animation

В `PlacesList` (LazyColumn) применить `Modifier.animateItem()` на каждой карточке — встроенный механизм анимации LazyColumn (Compose 1.7+):

```kotlin
items(items = places, key = { it.id }) { place ->
    PlaceCard(
        place = place,
        ...
        modifier = Modifier
            .fillMaxWidth()
            .animateItem(
                fadeInSpec = tween(durationMillis = LocalGuideDesignTokens.animationDurationMedium),
                placementSpec = spring(stiffness = Spring.StiffnessLow)
            )
    )
}
```

Для визуального stagger-эффекта при первом показе списка: использовать `LaunchedEffect` с индексом в отдельном composable `AnimatedPlaceCard`, который получает `index` и запускает `AnimatedVisibility` с `delayMillis = index * 60` (максимум 5 карточек × 60ms = 300ms суммарно):

```kotlin
@Composable
private fun AnimatedPlaceCard(
    place: Place,
    index: Int,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(place.id) {
        delay(minOf(index.toLong() * 60L, 300L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(LocalGuideDesignTokens.animationDurationMedium)) +
                slideInVertically(tween(LocalGuideDesignTokens.animationDurationMedium)) { it / 3 },
    ) {
        PlaceCard(place = place, onClick = onClick, onDeleteClick = onDeleteClick, modifier = modifier)
    }
}
```

### Extended FAB со сворачиванием

Заменить `FloatingActionButton` на `ExtendedFloatingActionButton`:

```kotlin
val listState = rememberLazyListState()
val isScrolled by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}

ExtendedFloatingActionButton(
    text = { Text(stringResource(R.string.places_list_fab_add)) },
    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
    onClick = onNavigateToAdd,
    expanded = !isScrolled,
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
)
```

`LazyColumn` должен получить `state = listState`.

Добавить строку: `<string name="places_list_fab_add_label">Добавить место</string>` в `strings.xml`.

### TopAppBar со scrollBehavior (в MainScreen)

Изменить `MainScreen.kt`: заменить статичный `TopAppBar` на `TopAppBar` с `enterAlwaysScrollBehavior`:

```kotlin
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_bar_title_places),
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    },
    ...
)
```

---

## Предусловия

- [ ] TASK-009 выполнена: `LocalGuidePlannerTheme`, `LocalGuideDesignTokens` (цвета категорий, cardElevation, тайминги), Shapes доступны

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|--------------------|
| `ui/places/common/PlaceCard.kt` | create | Новый файл с `PlaceCard` и `AnimatedPlaceCard` composable |
| `ui/places/common/PlaceCategoryExt.kt` | modify | Добавить `toIcon(): ImageVector` и `toAccentColor(): Color` |
| `ui/places/list/PlacesListScreen.kt` | modify | Использовать `AnimatedPlaceCard`, `ExtendedFloatingActionButton`, передать `listState` в LazyColumn |
| `ui/main/MainScreen.kt` | modify | `TopAppBar` с `enterAlwaysScrollBehavior`, `nestedScroll` modifier |

### Архитектурные решения

- `PlaceCard` — stateless composable, принимает `Place`, `onClick`, `onDeleteClick`, `modifier`.
- `AnimatedPlaceCard` — wrapper над `PlaceCard` с локальным анимационным состоянием.
- `listState` (`LazyListState`) — локальная переменная в `PlacesList`, передаётся в `LazyColumn`.
- `isScrolled` — `derivedStateOf` чтобы избежать лишних рекомпозиций.
- `PlaceCard` не знает о ViewModel, только о доменной модели `Place`.
- `toAccentColor()` использует `@Composable` annotation, так как возвращает `Color` из `LocalGuideDesignTokens` (который не @Composable — значит annotation не нужна, просто обычная extension fun).

### Необходимые зависимости

`material-icons-extended` — убедиться что подключена (см. TASK-010). Иконки `Icons.Rounded.*` входят в этот артефакт.

### Строки (strings.xml — добавить)

```xml
<string name="places_list_fab_add_label">Добавить место</string>
```

## Критерии приёма (Definition of Done)

- [ ] `PlaceCard.kt` создан в `ui/places/common/`
- [ ] Карточка отображает цветной квадрат с иконкой категории, название, адрес, chip категории
- [ ] У карточки тень (`ElevatedCard`, elevation 2.dp) и скруглённые углы 24.dp
- [ ] Цвет акцентного блока соответствует категории (7 разных цветов)
- [ ] `PlaceCategoryExt.kt` расширен: `toIcon()` и `toAccentColor()` реализованы для всех 7 категорий
- [ ] Карточки появляются с staggered-анимацией (fade + slideInVertically с задержкой по индексу)
- [ ] FAB — `ExtendedFloatingActionButton`, расширяется/сворачивается при скролле
- [ ] TopAppBar в `MainScreen` скрывается при скролле вниз, появляется при скролле вверх
- [ ] `PlacesListViewModel`, `PlacesListUiState` не изменены
- [ ] `@Preview` обновлены и рендерятся
- [ ] Нет hardcoded цветов вне токенов
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Внешний вид карточки:**
   - Открыть список с несколькими местами разных категорий.
   - Ожидаемый результат: у каждой карточки свой цвет акцентного блока слева, соответствующая иконка.

2. **Staggered-анимация:**
   - Открыть список с 5+ местами.
   - Ожидаемый результат: карточки появляются одна за другой с небольшой задержкой, плавно выезжая снизу.

3. **Extended FAB:**
   - На экране со списком FAB развёрнут (показывает иконку + текст «Добавить место»).
   - Прокрутить список вниз.
   - Ожидаемый результат: FAB плавно сворачивается до иконки.
   - Прокрутить вверх — ожидаемый результат: FAB плавно разворачивается.

4. **Скрытие TopAppBar:**
   - Прокрутить список вниз.
   - Ожидаемый результат: TopAppBar плавно скрывается, давая больше места списку.
   - Прокрутить вверх — TopAppBar появляется.

5. **Удаление:**
   - Нажать иконку удаления на карточке.
   - Ожидаемый результат: место удаляется, список обновляется. Анимация удаления (animateItem).

6. **Пустой список:**
   - Удалить все места.
   - Ожидаемый результат: отображается EmptyPlacesContent.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-011-places-list-ui
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-15 | todo | Tech Director | Задача создана |
| 2026-03-18 | review | Developer | Реализация завершена, PR открыт |
