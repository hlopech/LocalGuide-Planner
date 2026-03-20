# TASK-010: Онбординг — анимированный hero-экран с современным UI

**Автор:** Tech Director
**Дата создания:** 2026-03-15
**Тип:** refactor
**Приоритет:** High
**Спринт:** SPRINT-2
**Зависимости:** TASK-009

---

## Описание

Текущий `OnboardingScreen` — простая вертикальная колонка по центру экрана: Text + Text + OutlinedTextField + Button. Никаких анимаций, иллюстраций, визуальной иерархии.

Необходимо переработать визуальную часть `OnboardingContent` (только UI, логика ViewModel и UiState не меняются): добавить hero-секцию с градиентом, анимированное появление каждого элемента и красивое поле ввода.

**Важно:** функция `OnboardingScreen` (со слоем ViewModel) не меняется. Меняется только `OnboardingContent` и её дочерние composable.

### Целевой дизайн

Экран делится на две зоны:

**Зона 1 — Hero (верхние ~45% экрана):**
- `Box` с `fillMaxWidth()`, высота `0.45f` от `fillMaxHeight()`
- Фон: `Brush.verticalGradient` от `LocalGuideDesignTokens.heroGradientColors` (`#E8622A` → `#C0410D`)
- Скругление нижних углов: `RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)`
- Внутри по центру: иконка `Icons.Rounded.Explore` (или `Icons.Filled.Map`), размер `80.dp`, tint = `Color.White`, под ней — крупный Text заголовок приложения (`headlineLarge` из темы, цвет `Color.White`)
- Иконка и текст появляются с анимацией `AnimatedVisibility` + `fadeIn() + slideInVertically { -it / 2 }`, запускается сразу при входе (`LaunchedEffect(Unit) { visible = true }`)

**Зона 2 — Форма (оставшаяся часть):**
- `Column` с `padding(horizontal = 32.dp, top = 32.dp)`
- Подзаголовок: `bodyLarge`, цвет `MaterialTheme.colorScheme.onSurfaceVariant`
- Подзаголовок появляется с задержкой 150ms: `AnimatedVisibility(visible, enter = fadeIn(tween(300, delayMillis = 150)) + slideInVertically(tween(300, delayMillis = 150)) { it / 2 })`
- TextField появляется с задержкой 250ms (аналогичная анимация)
- Кнопка появляется с задержкой 350ms

**Поле ввода имени:**
- Использовать `OutlinedTextField` с кастомными `colors`: когда поле в фокусе — `focusedBorderColor = MaterialTheme.colorScheme.primary`, `focusedLabelColor = MaterialTheme.colorScheme.primary`
- `shape = MaterialTheme.shapes.medium` (16.dp скругление из TASK-009)
- Остальные параметры не меняются (`isError`, `supportingText`, `singleLine`)

**Кнопка «Продолжить»:**
- `Button` с `shape = MaterialTheme.shapes.extraLarge` (50.dp — pill-форма из TASK-009)
- Анимация нажатия через `scale`: использовать `animateFloatAsState` с `targetValue = if (isPressed) 0.95f else 1.0f`, применять через `Modifier.graphicsLayer { scaleX = scale; scaleY = scale }`. Состояние `isPressed` получать через `Modifier.pointerInput` + `detectTapGestures(onPress = { ... })`
- При состоянии `isLoading = true`: показывать `CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)` внутри Row вместо текста кнопки (через `Crossfade(targetState = uiState.isLoading)`)
- `modifier = Modifier.fillMaxWidth()`

---

## Предусловия

- [ ] TASK-009 выполнена: `LocalGuidePlannerTheme`, `LocalGuideDesignTokens`, новая палитра, Typography, Shapes доступны

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|--------------------|
| `ui/onboarding/OnboardingScreen.kt` | modify | Переработать `OnboardingContent` и дочерние composable согласно дизайну выше |

### Архитектурные решения

- ViewModel (`OnboardingViewModel`), UiState, UiEffect — **не трогать**. Только визуальный слой.
- Все анимационные состояния (`visible: Boolean`) — локальные переменные `remember { mutableStateOf(false) }` в `OnboardingContent`. Они не попадают в ViewModel.
- `AnimatedVisibility` — для каждого UI-блока отдельный, с нарастающей задержкой (`delayMillis`).
- `Crossfade` — для переключения кнопка/индикатор загрузки.
- `animateFloatAsState` + `graphicsLayer` — для scale-анимации кнопки при нажатии.
- Импортировать только из `androidx.compose.animation.*`, `androidx.compose.animation.core.*`.

### Структура composable

```
OnboardingContent(uiState, onNameChanged, onGetStartedClicked)
├── OnboardingHeroSection(visible: Boolean)          // новый private composable
│   ├── Box с градиентом и скруглёнными нижними углами
│   ├── Icon(Icons.Rounded.Explore) + AnimatedVisibility
│   └── Text(headlineLarge) + AnimatedVisibility
└── OnboardingFormSection(uiState, visible, callbacks) // новый private composable
    ├── AnimatedVisibility(subtitle) delayMillis=150
    ├── AnimatedVisibility(OnboardingTextField) delayMillis=250
    └── AnimatedVisibility(OnboardingButton) delayMillis=350

OnboardingTextField(value, onValueChange, error, modifier)  // новый private composable
OnboardingButton(isLoading, enabled, onClick, modifier)      // новый private composable
```

Каждый composable не длиннее 50 строк.

### Необходимые импорты (проверить наличие в build.gradle)

Все анимационные API входят в стандартный `androidx.compose.animation` — дополнительных зависимостей не требуется. `Icons.Rounded` входит в `material-icons-extended` — убедиться, что зависимость уже подключена в `build.gradle.kts` (app). Если нет — добавить:
```kotlin
implementation("androidx.compose.material:material-icons-extended")
```

### Строки (strings.xml)

Новых строк не требуется — все существующие строки используются как есть (`R.string.onboarding_title`, `R.string.onboarding_subtitle` и т.д.).

## Критерии приёма (Definition of Done)

- [ ] Hero-секция отображает градиентный блок со скруглёнными нижними углами, иконкой и заголовком
- [ ] Все элементы экрана появляются с fade + slide анимацией с нарастающей задержкой
- [ ] TextField имеет скруглённые углы (`shapes.medium`) и цветной border при фокусе
- [ ] Кнопка имеет pill-форму (`shapes.extraLarge`) и scale-анимацию при нажатии
- [ ] При `isLoading = true` — внутри кнопки показывается `CircularProgressIndicator`, переключение через `Crossfade`
- [ ] Логика ViewModel, UiState, UiEffect не изменена
- [ ] `@Preview` обновлены и отображают новый UI без ошибок (все 4 превью)
- [ ] Нет hardcoded цветов (все цвета через MaterialTheme или LocalGuideDesignTokens)
- [ ] Нет hardcoded строк
- [ ] Функции не длиннее 50 строк
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Анимация появления:**
   - Запустить приложение, перейти на экран онбординга.
   - Ожидаемый результат: hero-секция видна сразу, затем появляются подзаголовок (150ms), поле ввода (250ms), кнопка (350ms) с плавным fade + slide.

2. **Кнопка при вводе:**
   - Ввести имя в поле.
   - Нажать и удерживать кнопку «Продолжить».
   - Ожидаемый результат: кнопка слегка сжимается (scale ~0.95), отпустить — возвращается в исходный размер.

3. **Состояние загрузки:**
   - Нажать «Продолжить» (вызывает onGetStartedClicked).
   - Ожидаемый результат: текст кнопки плавно сменяется `CircularProgressIndicator` через Crossfade.

4. **Поле ввода в фокусе:**
   - Тапнуть по полю ввода имени.
   - Ожидаемый результат: граница поля и label меняют цвет на `primary` (терракотовый).

5. **Валидация:**
   - Нажать «Продолжить» с пустым полем (кнопка задизейблена — проверить недоступность).
   - Ожидаемый результат: кнопка визуально отображает disabled-состояние, клик не работает.

6. **Dark mode:**
   - Переключить в Dark mode.
   - Ожидаемый результат: hero-секция сохраняет тёмный вариант палитры, форма — тёмный фон.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-010-onboarding-ui
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-15 | todo | Tech Director | Задача создана |
| 2026-03-15 | review | Developer | Реализован анимированный hero-экран, открыт PR |
