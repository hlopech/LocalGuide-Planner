# TASK-005: Навигация — граф экранов, MainScreen, Bottom Navigation Bar

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** Critical
**Спринт:** SPRINT-1
**Зависимости:** TASK-001, TASK-002, TASK-003, TASK-004

---

## Описание

Реализовать навигационную инфраструктуру приложения: типобезопасный граф маршрутов (`Screen.kt`), основной `NavHost` в `MainActivity`, `MainScreen` со `Scaffold` и `BottomNavigationBar`, а также логику выбора стартового экрана (онбординг или главный экран) на основе флага `isOnboardingCompleted`.

Навигация — «скелет» приложения, к которому подключаются все остальные экраны.

## Предусловия

- [ ] TASK-001 выполнена: Navigation Compose подключён, Hilt настроен
- [ ] TASK-002 выполнена: `IsOnboardingCompletedUseCase` создан
- [ ] TASK-003 выполнена: Data-слой работает (UseCase корректно инжектируется)
- [ ] TASK-004 выполнена: `OnboardingScreen` существует и принимает навигационный колбэк

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/navigation/Screen.kt` | create | sealed class с маршрутами навигации |
| `ui/navigation/AppNavGraph.kt` | create | NavHost с полным графом экранов Sprint 1 |
| `ui/navigation/BottomNavBar.kt` | create | Composable — нижняя панель навигации |
| `ui/main/MainScreen.kt` | create | Scaffold + BottomNavBar + вложенный NavHost |
| `ui/main/MainViewModel.kt` | create | ViewModel для проверки состояния онбординга |
| `MainActivity.kt` | modify | Интегрировать AppNavGraph, Hilt, передать startDestination |

### Архитектурные решения

- **Screen.kt** — sealed class с `object`-маршрутами. Маршруты с аргументами используют `route` со строковой интерполяцией. Это стандартный паттерн для Navigation Compose до появления Safe Args для Compose.
- Определение стартового экрана: `MainActivity` через `MainViewModel` (или напрямую через `LaunchedEffect` в NavHost) вызывает `IsOnboardingCompletedUseCase` и устанавливает `startDestination`.
- `MainScreen` включает `Scaffold` с `TopAppBar` (заголовок зависит от текущей вкладки) и `BottomNavigationBar`.
- **Bottom Navigation**: в Sprint 1 отображается **только одна вкладка — «Места»**. Вкладки «Коллекции» и «Профиль» **скрыты** до Sprint 2. Решение подтверждено Product Owner 2026-03-14.
- Параметры навигации передаются через route-строки. Аргументы декодируются в принимающем экране.
- `hiltViewModel()` используется для инжекции ViewModel в Compose-функциях.

### Маршруты (Screen.kt)

```kotlin
// ui/navigation/Screen.kt
sealed class Screen(val route: String) {
    // Onboarding
    data object Onboarding : Screen("onboarding")

    // Main (container)
    data object Main : Screen("main")

    // Places
    data object PlacesList : Screen("places/list")

    data class PlaceDetail(val placeId: String = "{placeId}") :
        Screen("places/detail/$placeId") {
        companion object {
            const val ROUTE = "places/detail/{placeId}"
            const val ARG_PLACE_ID = "placeId"
        }
    }

    data object AddPlace : Screen("places/add")

    data class EditPlace(val placeId: String = "{placeId}") :
        Screen("places/edit/$placeId") {
        companion object {
            const val ROUTE = "places/edit/{placeId}"
            const val ARG_PLACE_ID = "placeId"
        }
    }
}
```

### Структура AppNavGraph

```kotlin
// ui/navigation/AppNavGraph.kt
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingCompleted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToPlaceDetail = { placeId ->
                    navController.navigate(Screen.PlaceDetail(placeId).route)
                },
                onNavigateToAddPlace = {
                    navController.navigate(Screen.AddPlace.route)
                },
                onNavigateToEditPlace = { placeId ->
                    navController.navigate(Screen.EditPlace(placeId).route)
                }
            )
        }
        composable(
            route = Screen.PlaceDetail.ROUTE,
            arguments = listOf(navArgument(Screen.PlaceDetail.ARG_PLACE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString(Screen.PlaceDetail.ARG_PLACE_ID) ?: return@composable
            PlaceDetailScreen(placeId = placeId, onNavigateBack = { navController.popBackStack() }, ...)
        }
        // ... аналогично AddPlace, EditPlace
    }
}
```

### MainScreen и Bottom Navigation

```kotlin
// Вкладки Bottom Navigation
enum class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    PLACES("places/list", R.string.nav_places, Icons.Default.Place)
    // COLLECTIONS и PROFILE скрыты до Sprint 2 (решение Product Owner 2026-03-14)
}
```

### MainViewModel

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val isOnboardingCompletedUseCase: IsOnboardingCompletedUseCase
) : ViewModel() {
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = isOnboardingCompletedUseCase()
            _startDestination.value = if (completed) Screen.Main.route else Screen.Onboarding.route
        }
    }
}
```

### Изменения в MainActivity

- Добавить `@AndroidEntryPoint`
- Инжектировать `MainViewModel` через `hiltViewModel()` или `viewModels()`
- Ожидать `startDestination` (показывать SplashScreen или пустой экран пока `null`)
- Запускать `AppNavGraph` с корректным `startDestination`

### Строки для strings.xml (добавить)

```xml
<string name="nav_places">Места</string>
<string name="nav_collections">Коллекции</string>
<string name="nav_profile">Профиль</string>
<string name="app_bar_title_places">Мои места</string>
```

## Критерии приёма (Definition of Done)

- [ ] При первом запуске открывается `OnboardingScreen`
- [ ] После завершения онбординга открывается `MainScreen` с Bottom Navigation
- [ ] При повторном запуске сразу открывается `MainScreen`
- [ ] Bottom Navigation содержит только вкладку «Места» (остальные скрыты до Sprint 2)
- [ ] `MainScreen` содержит `Scaffold` с `TopAppBar` и `BottomNavigationBar`
- [ ] Навигация к `PlaceDetailScreen`, `AddEditPlaceScreen` работает (экраны могут быть заглушками — placeholder Composable с текстом)
- [ ] Кнопка «Назад» на устройстве корректно возвращает на предыдущий экран
- [ ] Маршрут `Onboarding` удаляется из back stack после перехода на `Main` (popUpTo inclusive)
- [ ] `MainViewModel` не импортирует классы из `data/`
- [ ] Unit-тест для `MainViewModel`: проверка корректного `startDestination` при `isOnboardingCompleted = true/false`
- [ ] Есть `@Preview` для `BottomNavBar` и `MainScreen`
- [ ] Все строки в `strings.xml`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — первый запуск:**
   - Установить приложение на чистый эмулятор.
   - Ожидаемый результат: отображается `OnboardingScreen`, не `MainScreen`.

2. **Счастливый путь — навигация после онбординга:**
   - Завершить онбординг (ввести имя, нажать «Начать»).
   - Ожидаемый результат: переход на `MainScreen` с Bottom Navigation, `OnboardingScreen` недоступен через кнопку «Назад».

3. **Счастливый путь — повторный запуск:**
   - Закрыть и открыть приложение после завершения онбординга.
   - Ожидаемый результат: сразу `MainScreen`, онбординг не показывается.

4. **Граничный случай — Bottom Navigation в Sprint 1:**
   - Убедиться, что в Bottom Navigation видна только вкладка «Места».
   - Ожидаемый результат: «Коллекции» и «Профиль» не отображаются.

5. **Граничный случай — кнопка «Назад» на главном экране:**
   - Находясь на `PlacesList`, нажать системную кнопку «Назад».
   - Ожидаемый результат: приложение сворачивается (нет пустого back stack).

---

## Статус выполнения

**Статус:** todo

**Ветка:** feature/TASK-005-navigation (заполняет Developer)
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
