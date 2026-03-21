# TASK-017: Карта мест (OSMDroid интеграция)

**Автор:** Tech Director
**Дата создания:** 2026-03-21
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-3
**Зависимости:** TASK-016

---

## Описание

Добавить четвёртую вкладку «Карта» — интерактивную карту с маркерами всех мест. Использовать OSMDroid (OpenStreetMap) — бесплатная карта без API-ключа. Маркеры кастомные: цвет соответствует категории места. Тап на маркер → навигация на `PlaceDetailScreen`. Задача зависит от TASK-016, потому что BottomNavBar уже имеет три вкладки и шаблон добавления устоялся.

### Целевой дизайн

- Четвёртая вкладка «Карта» в BottomNavBar, иконка `Icons.Rounded.Map`
- Полноэкранная карта OSMDroid через `AndroidView { MapView(context) }`
- При загрузке — `CircularProgressIndicator` поверх карты
- Маркеры: круглый цветной маркер с иконкой категории (цвет через `category.toAccentColor()`)
- При тапе на маркер — `InfoWindow` с названием места или немедленная навигация на `PlaceDetailScreen`
- Если у места `latitude == null || longitude == null` — маркер не добавляется
- Центрирование при первом открытии: на первое место из списка с координатами; если таких нет — Москва (55.7558, 37.6173), zoom = 10
- Fab «Моё местоположение» (опционально, только если включить `ACCESS_FINE_LOCATION` — см. ниже)

---

## Предусловия

- [ ] TASK-016 выполнена: BottomNavBar имеет три вкладки
- [ ] `GetPlacesUseCase` возвращает `Flow<List<Place>>`
- [ ] `Place` содержит `latitude: Double?` и `longitude: Double?`
- [ ] `PlaceCategoryExt.toAccentColor()` доступен (внутренний в пакете `ui/places/common`)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `android core/app/build.gradle.kts` | modify | Добавить `implementation("org.osmdroid:osmdroid-android:6.1.20")` |
| `android core/app/src/main/AndroidManifest.xml` | modify | Добавить `INTERNET` и `ACCESS_NETWORK_STATE` permissions, настроить `osmdroid` |
| `ui/map/MapUiState.kt` | create | Data class UiState |
| `ui/map/MapViewModel.kt` | create | HiltViewModel, подписка на места |
| `ui/map/MapScreen.kt` | create | Compose-экран с `AndroidView { MapView }` |
| `ui/navigation/Screen.kt` | modify | Добавить `data object Map : Screen("map")` |
| `ui/main/MainScreen.kt` | modify | Добавить четвёртую вкладку «Карта» |

### Архитектурные решения

**Зависимость osmdroid:**

```kotlin
// android core/app/build.gradle.kts
implementation("org.osmdroid:osmdroid-android:6.1.20")
```

Проверить совместимость версии osmdroid с minSdk 24. Версия 6.1.20 поддерживает API 21+.

**AndroidManifest.xml:**

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

`ACCESS_FINE_LOCATION` — **не добавлять** в рамках этой задачи (требует обработки runtime permissions — усложняет задачу). Кнопка «Моё местоположение» помечается как вне скопа.

Для osmdroid необходимо инициализировать `Configuration` в `Application` классе (или в `MapScreen` через `DisposableEffect`):
```kotlin
// В MapScreen или LocalGuidePlannerApplication
Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
Configuration.getInstance().userAgentValue = context.packageName
```

**MapUiState:**

```kotlin
data class MapUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
```

**MapViewModel:**

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
) : ViewModel() {

    val uiState: StateFlow<MapUiState> = getPlacesUseCase()
        .map { places -> MapUiState(places = places, isLoading = false) }
        .catch { e -> emit(MapUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(isLoading = true),
        )
}
```

**MapScreen — ключевые решения:**

```
MapScreen(onNavigateToDetail, modifier, viewModel)
    ↓ collectAsStateWithLifecycle
MapContent(uiState, onNavigateToDetail, modifier)
    └── Box(fillMaxSize)
        ├── OsmMapView(places, onPlaceClick, modifier = fillMaxSize)  ← AndroidView
        └── if (isLoading) CircularProgressIndicator(Alignment.Center)
```

`OsmMapView` — отдельный composable с `AndroidView`:

```kotlin
@Composable
fun OsmMapView(
    places: List<Place>,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(10.0)
                // Центрирование на первом месте или Москве
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            places
                .filter { it.latitude != null && it.longitude != null }
                .forEach { place ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(place.latitude!!, place.longitude!!)
                    marker.title = place.name
                    marker.setOnMarkerClickListener { _, _ ->
                        onPlaceClick(place.id)
                        true
                    }
                    // Кастомный цвет маркера через Drawable с цветом категории
                    mapView.overlays.add(marker)
                }
            mapView.invalidate()
        },
        modifier = modifier,
    )
}
```

**Управление жизненным циклом MapView:**

`MapView` требует вызова `onResume()` и `onPause()` в соответствующих lifecycle-событиях. Использовать `DisposableEffect`:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> Unit
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

Для этого `mapView` должен быть доступен в `DisposableEffect` — хранить его в `remember`.

**Кастомные маркеры (цвет категории):**

Простой подход — использовать `Marker.setIcon(Drawable)`. Создавать `GradientDrawable` программно:

```kotlin
fun createMarkerDrawable(context: Context, color: Color): Drawable {
    val size = context.resources.getDimensionPixelSize(/* 32dp */)
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color.toArgb())
        setSize(size, size)
    }
}
```

Вызывать `marker.setIcon(createMarkerDrawable(context, place.category.toAccentColor()))`.

**Важно:** `toAccentColor()` объявлен как `internal` в пакете `ui/places/common`. Файл `MapScreen.kt` будет находиться в пакете `ui/map`. Необходимо либо:
- Переместить файл `MapScreen.kt` в пакет `ui/places/map/` (тогда он в другом пакете, `internal` не поможет)
- Или создать вспомогательный файл-расширение `MapPlaceCategoryExt.kt` в `ui/map/` с публичной функцией
- **Рекомендуемое решение:** создать `ui/map/MapPlaceCategoryExt.kt` с `internal fun PlaceCategory.toMapMarkerColor(): Color` — дублирует `toAccentColor()` но сохраняет инкапсуляцию

Альтернатива: сделать `toAccentColor()` в `PlaceCategoryExt.kt` `public` вместо `internal` — обсудить с Tech Director при необходимости. В рамках данной задачи Developer решает самостоятельно и фиксирует решение в PR-описании.

**Добавление вкладки в MainScreen:**

```kotlin
BottomNavItem(Screen.Map, R.string.nav_map, Icons.Rounded.Map),
```

В `NavHost`:
```kotlin
composable(route = Screen.Map.route) {
    MapScreen(onNavigateToDetail = onNavigateToDetail)
}
```

Заголовок TopAppBar: `R.string.app_bar_title_map`.

**Потенциальный риск:** `AndroidView` с `MapView` не поддерживает `@Preview` — использовать заглушку в preview.

### Строки для добавления в strings.xml

```xml
<string name="nav_map">Карта</string>
<string name="app_bar_title_map">Карта мест</string>
<string name="map_error_loading">Не удалось загрузить карту</string>
<string name="map_empty_no_coordinates">Нет мест с координатами</string>
```

### Зависимости

```kotlin
// android core/app/build.gradle.kts
implementation("org.osmdroid:osmdroid-android:6.1.20")
```

Permissions в AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

Тайлы OSMDroid кэшируются в `osmdroid/` в internal storage — дополнительной конфигурации не требует.

### Риски и ограничения

| Риск | Вероятность | Митигация |
|------|-------------|-----------|
| `AndroidView` переиициализируется при рекомпозиции | Высокая | Хранить `MapView` в `remember { MapView(context) }` |
| Маркеры не обновляются при изменении списка | Средняя | Вызывать `mapView.overlays.clear()` + повторное добавление в `update` блоке `AndroidView` |
| `toAccentColor()` недоступен из другого пакета (`internal`) | Высокая | Создать `MapPlaceCategoryExt.kt` в `ui/map/` |
| Preview не работает с `MapView` | Высокая | Обернуть в условие `if (LocalInspectionMode.current)` для preview-заглушки |
| Карта не загружается без INTERNET permission | Высокая | Добавить permission в Manifest до запуска |

## Критерии приёма (Definition of Done)

- [ ] `osmdroid-android:6.1.20` добавлен в `build.gradle.kts`
- [ ] `INTERNET` и `ACCESS_NETWORK_STATE` добавлены в `AndroidManifest.xml`
- [ ] `MapUiState` создан в `ui/map/`
- [ ] `MapViewModel` создан, подписывается на `GetPlacesUseCase`
- [ ] `MapScreen` создан с `AndroidView { MapView }` через `OsmMapView` composable
- [ ] `osmdroid Configuration` инициализируется с `userAgentValue = packageName`
- [ ] `MapView` корректно вызывает `onResume()`/`onPause()` через `DisposableEffect`
- [ ] Все места с `latitude != null && longitude != null` отображаются маркерами
- [ ] Места с `null` координатами — без маркеров
- [ ] Маркеры имеют уникальный цвет по категории
- [ ] Тап на маркер → навигация на `PlaceDetailScreen` нужного места
- [ ] Карта центрируется на первом месте с координатами (или Москва при отсутствии)
- [ ] Четвёртая вкладка «Карта» добавлена в BottomNavBar
- [ ] `Screen.Map` добавлен в `Screen.kt`
- [ ] `@Preview` для `MapScreen` показывает заглушку (без реального MapView)
- [ ] Unit-тесты для `MapViewModel`: загрузка мест, обработка ошибки
- [ ] Нет hardcoded строк
- [ ] KTLint без ошибок
- [ ] Функции не длиннее 50 строк

## Тест-сценарии для QA

1. **Отображение карты:**
   - Открыть вкладку «Карта». При наличии INTERNET подождать загрузки тайлов.
   - Ожидаемый результат: карта OSM загружается, отображаются маркеры для мест с координатами.

2. **Маркеры по категории:**
   - Добавить места разных категорий с координатами. Открыть карту.
   - Ожидаемый результат: маркеры имеют разные цвета соответственно категориям.

3. **Тап на маркер:**
   - Нажать на маркер места на карте.
   - Ожидаемый результат: открывается `PlaceDetailScreen` данного места.

4. **Место без координат:**
   - Добавить место без указания координат (latitude/longitude = null). Открыть карту.
   - Ожидаемый результат: для данного места маркер не отображается.

5. **Центрирование карты:**
   - При первом открытии карты при наличии мест с координатами.
   - Ожидаемый результат: карта отцентрирована на первом месте из списка с координатами.

6. **Центрирование по умолчанию:**
   - При отсутствии мест с координатами открыть карту.
   - Ожидаемый результат: карта отцентрирована на Москве (55.7558, 37.6173), zoom ≈ 10.

7. **Обновление маркеров:**
   - Добавить новое место с координатами через вкладку «Места». Переключиться на «Карту».
   - Ожидаемый результат: новый маркер появляется на карте без перезапуска приложения.

8. **Жизненный цикл карты:**
   - Свернуть приложение (HOME), вернуться.
   - Ожидаемый результат: карта возобновляет работу корректно, тайлы отображаются.

9. **Навигация по вкладкам:**
   - Переключиться с «Карты» на другую вкладку и обратно.
   - Ожидаемый результат: карта восстанавливает состояние (позиция, zoom).

---

## Статус выполнения

**Статус:** todo

**Ветка:** feature/TASK-017-map-screen (заполняет Developer)
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-21 | todo | Tech Director | Задача создана |
