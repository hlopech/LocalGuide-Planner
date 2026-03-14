# TASK-004: Онбординг — экран приветствия и локальный профиль пользователя

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** Critical
**Спринт:** SPRINT-1
**Зависимости:** TASK-002, TASK-003

---

## Описание

Реализовать экран онбординга, который показывается при первом запуске приложения. Пользователь вводит своё имя и нажимает кнопку «Начать». Данные сохраняются через `SaveUserProfileUseCase` в DataStore. При повторных запусках онбординг пропускается — приложение сразу открывает главный экран.

Цель: создать первое взаимодействие пользователя с приложением, сформировать локальный профиль и заложить архитектурную основу для будущей интеграции Firebase Auth.

## Предусловия

- [ ] TASK-002 выполнена: `SaveUserProfileUseCase`, `IsOnboardingCompletedUseCase` созданы
- [ ] TASK-003 выполнена: `UserProfileRepositoryImpl`, Hilt-модули работают

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `ui/onboarding/OnboardingViewModel.kt` | create | ViewModel с StateFlow и логикой онбординга |
| `ui/onboarding/OnboardingScreen.kt` | create | Compose-экран ввода имени пользователя |
| `ui/onboarding/OnboardingUiState.kt` | create | Data class состояния экрана |

### Архитектурные решения

- **OnboardingViewModel** управляет состоянием формы через `StateFlow<OnboardingUiState>`.
- Навигация после успешного сохранения профиля передаётся через `SharedFlow<OnboardingUiEffect>` (side effect), а не через UiState — это отделяет навигационные события от состояния UI.
- Валидация имени: не пустое, от 1 до 50 символов. Ошибки валидации отражаются в `UiState`.
- ViewModel **не** использует никаких Android-зависимостей, кроме `ViewModel`. Навигационный callback не передаётся во ViewModel.
- Онбординг проверяет завершение только один раз при старте через `IsOnboardingCompletedUseCase` — это ответственность `MainActivity` или NavGraph (описано в TASK-005).

### UiState и UiEffect

```kotlin
// ui/onboarding/OnboardingUiState.kt
data class OnboardingUiState(
    val name: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false
)

sealed class OnboardingUiEffect {
    data object NavigateToMain : OnboardingUiEffect()
}
```

### Структура ViewModel

```kotlin
// ui/onboarding/OnboardingViewModel.kt
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OnboardingUiEffect>()
    val uiEffect: SharedFlow<OnboardingUiEffect> = _uiEffect.asSharedFlow()

    fun onNameChanged(name: String) { /* обновить _uiState */ }

    fun onContinueClicked() {
        // Валидация
        // viewModelScope.launch -> saveUserProfileUseCase -> emit NavigateToMain
    }
}
```

### Экран (ключевые требования)

- Центрированный экран с логотипом/иллюстрацией (placeholder), полем ввода имени и кнопкой «Начать».
- Поле ввода: `OutlinedTextField` с `label = "Ваше имя"`, отображение `nameError` через `supportingText`.
- Кнопка неактивна (`enabled = false`) пока поле пустое.
- При `isLoading = true` показывается `CircularProgressIndicator` вместо кнопки.
- Навигационные события от `uiEffect` обрабатываются в `LaunchedEffect`.
- Каждый публичный `@Composable` имеет `@Preview`.
- Строки через `strings.xml`, не hardcoded.

### Строки для strings.xml (добавить)

```xml
<string name="onboarding_title">Добро пожаловать!</string>
<string name="onboarding_subtitle">Как вас зовут?</string>
<string name="onboarding_name_label">Ваше имя</string>
<string name="onboarding_name_error_empty">Введите имя</string>
<string name="onboarding_name_error_too_long">Имя не должно превышать 50 символов</string>
<string name="onboarding_button_continue">Начать</string>
```

## Критерии приёма (Definition of Done)

- [ ] `OnboardingScreen` отображается при первом запуске
- [ ] Поле ввода имени работает корректно: обновляет состояние при вводе
- [ ] Кнопка «Начать» недоступна при пустом поле
- [ ] При коротком/пустом имени отображается ошибка валидации
- [ ] При успешном сохранении эмитится `OnboardingUiEffect.NavigateToMain`
- [ ] `OnboardingViewModel` не импортирует классы из `data/`
- [ ] `isLoading` корректно управляет видимостью индикатора загрузки
- [ ] Unit-тесты для `OnboardingViewModel`: проверка валидации, проверка вызова UseCase, проверка эмиссии NavigateToMain
- [ ] Все строки в `strings.xml`
- [ ] Есть `@Preview` для `OnboardingScreen`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — ввод имени и продолжение:**
   - Запустить приложение на чистом устройстве/эмуляторе (без сохранённых данных).
   - Ввести имя «Алексей», нажать «Начать».
   - Ожидаемый результат: приложение переходит на главный экран, имя сохранено.

2. **Повторный запуск — пропуск онбординга:**
   - Перезапустить приложение после завершения онбординга.
   - Ожидаемый результат: онбординг-экран не показывается, сразу открывается главный экран.

3. **Граничный случай — пустое поле:**
   - Открыть экран, ничего не вводить, нажать «Начать».
   - Ожидаемый результат: кнопка недоступна или отображается ошибка «Введите имя».

4. **Граничный случай — имя из 51 символа:**
   - Ввести строку длиной 51 символ.
   - Ожидаемый результат: отображается ошибка «Имя не должно превышать 50 символов».

5. **Негативный сценарий — пробелы в имени:**
   - Ввести строку из одних пробелов.
   - Ожидаемый результат: валидация не проходит (пробелы триммируются перед проверкой).

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-004-onboarding
**PR:** — (ожидает создания через gh pr create)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-14 | review | Developer | Реализация завершена, тесты зелёные (47/47), assembleDebug успешен |
