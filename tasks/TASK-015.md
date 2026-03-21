# TASK-015: Экран профиля пользователя

**Автор:** Tech Director
**Дата создания:** 2026-03-21
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-3
**Зависимости:** TASK-014

---

## Описание

Добавить экран «Профиль» — вторую вкладку BottomNavBar. Экран показывает имя пользователя, статистику по местам, позволяет редактировать имя и сбросить все данные. Это первая задача, которая расширяет BottomNavBar — после её выполнения последующие задачи (TASK-016, TASK-017) будут добавлять новые вкладки по тому же шаблону.

### Целевой дизайн

**Hero-секция:**
- `Box` высотой `LocalGuideDesignTokens.heroSectionHeight` (200.dp) с горизонтальным градиентным фоном (`Brush.horizontalGradient(LocalGuideDesignTokens.heroGradientColors)`)
- По центру вертикально: аватар-заглушка `Icon(Icons.Rounded.Person)` размером 80.dp белого цвета, под ним имя пользователя стилем `MaterialTheme.typography.headlineMedium` белого цвета

**Карточки статистики:**
- `ElevatedCard` с `LazyRow` (или обычный `Row`) с тремя карточками: «Всего мест» (count), «Избранных» (count isFavorite), «Топ категория» (самая частая категория)
- Каждая карточка: число стилем `headlineSmall`, подпись стилем `labelMedium`

**Редактирование имени:**
- `LocalGuideTextField` (из `ui/places/common/LocalGuideTextField.kt`) с текущим именем, `leadingIcon = Icons.Rounded.Person`
- Кнопка «Сохранить» активна только когда имя изменено и не пустое

**Кнопка «Сбросить данные»:**
- `OutlinedButton` с `colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)`
- При нажатии — `AlertDialog` с подтверждением: «Удалить все места? Это действие нельзя отменить.»
- При подтверждении вызывает `DeleteAllPlacesUseCase`

---

## Предусловия

- [ ] TASK-014 выполнена: BottomNavBar стабилен, навигация работает
- [ ] `GetUserProfileUseCase`, `SaveUserProfileUseCase` существуют и работают
- [ ] `GetPlacesUseCase` возвращает `Flow<List<Place>>`
- [ ] `LocalGuideTextField` существует в `ui/places/common/`
- [ ] `LocalGuideDesignTokens.heroGradientColors` и `heroSectionHeight` доступны

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `domain/usecase/place/DeleteAllPlacesUseCase.kt` | create | Новый UseCase: `suspend operator fun invoke() = repository.deleteAllPlaces()` |
| `domain/repository/PlacesRepository.kt` | modify | Добавить метод `suspend fun deleteAllPlaces()` |
| `data/local/db/place/PlaceDao.kt` | modify | Добавить `@Query("DELETE FROM places") suspend fun deleteAllPlaces()` |
| `data/repository/PlacesRepositoryImpl.kt` | modify | Реализовать `override suspend fun deleteAllPlaces() = dao.deleteAllPlaces()` |
| `ui/profile/ProfileUiState.kt` | create | Data class с полями UiState |
| `ui/profile/ProfileViewModel.kt` | create | HiltViewModel, подписка на профиль и список мест |
| `ui/profile/ProfileScreen.kt` | create | Compose-экран с Hero, статистикой, редактором имени, кнопкой сброса |
| `ui/navigation/Screen.kt` | modify | Добавить `data object Profile : Screen("profile")` |
| `ui/main/MainScreen.kt` | modify | Добавить вторую вкладку «Профиль» в BottomNavBar и маршрут в NavHost |

### Архитектурные решения

**ProfileUiState:**

```kotlin
data class ProfileUiState(
    val userName: String = "",
    val editedName: String = "",
    val totalPlacesCount: Int = 0,
    val favoritePlacesCount: Int = 0,
    val topCategory: PlaceCategory? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val errorMessage: String? = null,
)
```

**ProfileViewModel:**

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val getPlacesUseCase: GetPlacesUseCase,
    private val deleteAllPlacesUseCase: DeleteAllPlacesUseCase,
) : ViewModel()
```

Комбинировать `getUserProfileUseCase()` и `getPlacesUseCase()` через `combine` для единого `uiState: StateFlow<ProfileUiState>`.

Статистика вычисляется в ViewModel:
```kotlin
val topCategory = places
    .groupBy { it.category }
    .maxByOrNull { it.value.size }
    ?.key
```

Функции:
- `fun onNameChanged(name: String)` — обновляет `editedName` через отдельный `MutableStateFlow<String>`
- `fun saveProfile()` — вызывает `saveUserProfileUseCase`, устанавливает `isSaving`
- `fun showResetDialog()` / `fun dismissResetDialog()`
- `fun resetAllData()` — вызывает `deleteAllPlacesUseCase`, закрывает диалог

**MainScreen — расширение BottomNavBar:**

Добавить data class или sealed class для описания вкладок, чтобы избежать дублирования:

```kotlin
private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.PlacesList, R.string.nav_places, Icons.Filled.Place),
    BottomNavItem(Screen.Profile, R.string.nav_profile, Icons.Rounded.Person),
)
```

`TopAppBar` заголовок должен меняться в зависимости от текущей вкладки:
- `PlacesList` → `R.string.app_bar_title_places`
- `Profile` → `R.string.app_bar_title_profile`

**Важно:** `ProfileScreen` принимает только `modifier: Modifier` — нет навигационных колбеков (экран замкнутый).

**Важно:** `DeleteAllPlacesUseCase` — suspend-функция, не Flow. Вызывается в `viewModelScope.launch`.

### API / База данных

Новый метод DAO:

```kotlin
@Query("DELETE FROM places")
suspend fun deleteAllPlaces()
```

Room выполняет DELETE без возврата значения. Метод добавляется в существующий `PlaceDao`, не требует миграции схемы (только DML, не DDL).

**Внимание:** добавление нового метода в DAO и Repository изменяет интерфейс `PlacesRepository`. После изменения интерфейса необходимо убедиться, что `PlacesRepositoryImpl` компилируется (реализует все методы).

### Строки для добавления в strings.xml

```xml
<string name="nav_profile">Профиль</string>
<string name="app_bar_title_profile">Профиль</string>
<string name="profile_stat_total_places">Всего мест</string>
<string name="profile_stat_favorites">Избранных</string>
<string name="profile_stat_top_category">Топ категория</string>
<string name="profile_stat_top_category_none">—</string>
<string name="profile_edit_name_label">Ваше имя</string>
<string name="profile_save_button">Сохранить</string>
<string name="profile_reset_button">Сбросить все данные</string>
<string name="profile_reset_dialog_title">Удалить все места?</string>
<string name="profile_reset_dialog_message">Это действие нельзя отменить. Все сохранённые места будут удалены.</string>
```

### Зависимости (build.gradle.kts)

Новые зависимости не требуются.

## Критерии приёма (Definition of Done)

- [ ] `DeleteAllPlacesUseCase` создан в `domain/usecase/place/`
- [ ] `PlacesRepository` содержит метод `deleteAllPlaces()`
- [ ] `PlaceDao` содержит `@Query("DELETE FROM places") suspend fun deleteAllPlaces()`
- [ ] `PlacesRepositoryImpl` реализует `deleteAllPlaces()`
- [ ] `ProfileUiState` создан с указанными полями
- [ ] `ProfileViewModel` создан, использует `combine` для `getUserProfileUseCase()` и `getPlacesUseCase()`
- [ ] Hero-секция отображается с градиентом `LocalGuideDesignTokens.heroGradientColors`
- [ ] Отображаются три карточки статистики: всего мест, избранных, топ-категория
- [ ] `LocalGuideTextField` используется для редактирования имени
- [ ] Кнопка «Сохранить» активна только при изменённом непустом имени
- [ ] `AlertDialog` показывается перед сбросом данных
- [ ] После сброса список мест очищается
- [ ] Вторая вкладка «Профиль» добавлена в BottomNavBar с иконкой `Icons.Rounded.Person`
- [ ] `Screen.Profile` добавлен в `Screen.kt`
- [ ] Навигация по вкладкам сохраняет состояние (`saveState = true`, `restoreState = true`)
- [ ] Заголовок TopAppBar меняется при переключении вкладок
- [ ] Unit-тесты для `ProfileViewModel`: загрузка профиля, изменение имени, сохранение, подсчёт статистики, deleteAll
- [ ] `@Preview` для ProfileScreen: с данными, загрузка, диалог подтверждения
- [ ] Нет hardcoded строк и цветов
- [ ] KTLint без ошибок
- [ ] Функции не длиннее 50 строк

## Тест-сценарии для QA

1. **Переключение вкладок:**
   - Нажать на вкладку «Профиль» в BottomNavBar.
   - Ожидаемый результат: открывается ProfileScreen, заголовок TopAppBar меняется на «Профиль». Вернуться на «Места» — список сохранён.

2. **Hero-секция:**
   - Открыть ProfileScreen.
   - Ожидаемый результат: градиентная секция с аватаром и именем пользователя из онбординга.

3. **Статистика:**
   - Добавить несколько мест разных категорий, часть пометить избранными. Открыть профиль.
   - Ожидаемый результат: корректно отображается общее число мест, число избранных, наиболее частая категория.

4. **Редактирование имени:**
   - Изменить имя в поле. Нажать «Сохранить».
   - Ожидаемый результат: имя обновляется в Hero-секции. Кнопка «Сохранить» становится неактивной (имя совпадает с сохранённым).

5. **Валидация имени:**
   - Очистить поле имени. Убедиться, что кнопка «Сохранить» недоступна.
   - Ожидаемый результат: кнопка `enabled = false` при пустом поле.

6. **Сброс данных — отмена:**
   - Нажать «Сбросить все данные». В диалоге нажать «Отмена».
   - Ожидаемый результат: диалог закрывается, данные не удалены.

7. **Сброс данных — подтверждение:**
   - Нажать «Сбросить все данные». Подтвердить.
   - Ожидаемый результат: диалог закрывается, переключиться на «Места» — список пуст, показывается `EmptyPlacesContent`.

8. **Нет топ-категории:**
   - При пустом списке мест открыть профиль.
   - Ожидаемый результат: в карточке «Топ категория» отображается «—» (строка `profile_stat_top_category_none`).

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-015-profile-screen
**PR:** (см. GitHub)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-21 | todo | Tech Director | Задача создана |
| 2026-03-21 | review | Developer | Реализованы все компоненты, тесты пройдены |
