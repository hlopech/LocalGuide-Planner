# TASK-009: Дизайн-система — кастомная тема Material3 для LocalGuide Planner

**Автор:** Tech Director
**Дата создания:** 2026-03-15
**Тип:** refactor
**Приоритет:** Critical
**Спринт:** SPRINT-2
**Зависимости:** нет

---

## Описание

Текущая тема приложения использует дефолтную фиолетовую палитру Material3 (Purple80/Purple40) и только один стиль типографики `bodyLarge`. Динамические цвета (Android 12+) перекрывают любую кастомную палитру, что делает брендинг невозможным.

Необходимо создать полноценную дизайн-систему: тёплую палитру путешественника, полную типографическую шкалу, кастомные скругления и служебные токены (градиенты, тени, цвета по категориям). Все последующие UI-задачи (TASK-010 — TASK-013) опираются на эту систему.

### Целевая палитра (тёплый «путешественник»)

Seed color: `#E8622A` (глубокий терракотовый/коралловый).

Полная схема для Light-темы:
| Роль | Hex | Описание |
|------|-----|----------|
| primary | `#C0410D` | Основной акцент — терракот |
| onPrimary | `#FFFFFF` | Текст на primary |
| primaryContainer | `#FFDBCF` | Светлый контейнер primary |
| onPrimaryContainer | `#3B0D00` | Текст в primaryContainer |
| secondary | `#4A6741` | Природный зелёный |
| onSecondary | `#FFFFFF` | |
| secondaryContainer | `#CCEDBE` | |
| onSecondaryContainer | `#082006` | |
| tertiary | `#3D6474` | Спокойный синий-серый |
| onTertiary | `#FFFFFF` | |
| tertiaryContainer | `#C1E9FB` | |
| onTertiaryContainer | `#001F29` | |
| error | `#BA1A1A` | |
| background | `#FFF8F6` | Тёплый белый фон |
| surface | `#FFF8F6` | |
| surfaceVariant | `#F5DED7` | |
| onSurfaceVariant | `#53433F` | |
| outline | `#857370` | |

Полная схема для Dark-темы:
| Роль | Hex |
|------|-----|
| primary | `#FFB59D` |
| onPrimary | `#671E00` |
| primaryContainer | `#8E2D00` |
| onPrimaryContainer | `#FFDBCF` |
| secondary | `#B1D1A4` |
| onSecondary | `#1C3817` |
| secondaryContainer | `#334F2B` |
| onSecondaryContainer | `#CCEDBE` |
| tertiary | `#A5CCDF` |
| onTertiary | `#073544` |
| tertiaryContainer | `#244C5B` |
| onTertiaryContainer | `#C1E9FB` |
| background | `#201A18` |
| surface | `#201A18` |
| surfaceVariant | `#53433F` |
| onSurfaceVariant | `#D8C2BC` |

### Типографика

Использовать системный шрифт `FontFamily.Default` (Roboto на Android). Определить полную шкалу:

| Стиль | Размер | Weight | LetterSpacing | LineHeight | Применение |
|-------|--------|--------|---------------|------------|------------|
| displayLarge | 57.sp | W400 | -0.25.sp | 64.sp | Не используется |
| displayMedium | 45.sp | W400 | 0.sp | 52.sp | Не используется |
| displaySmall | 36.sp | W400 | 0.sp | 44.sp | Не используется |
| headlineLarge | 32.sp | W700 | 0.sp | 40.sp | Hero-заголовки онбординга |
| headlineMedium | 28.sp | W600 | 0.sp | 36.sp | Заголовок TopAppBar |
| headlineSmall | 24.sp | W600 | 0.sp | 32.sp | Пустой список |
| titleLarge | 22.sp | W600 | 0.sp | 28.sp | Имя места в карточке |
| titleMedium | 16.sp | W600 | 0.15.sp | 24.sp | Подзаголовки секций |
| titleSmall | 14.sp | W600 | 0.1.sp | 20.sp | Метки категорий |
| bodyLarge | 16.sp | W400 | 0.15.sp | 24.sp | Основной текст |
| bodyMedium | 14.sp | W400 | 0.25.sp | 20.sp | Описание места |
| bodySmall | 12.sp | W400 | 0.4.sp | 16.sp | Вспомогательный текст |
| labelLarge | 14.sp | W600 | 0.1.sp | 20.sp | Кнопки |
| labelMedium | 12.sp | W600 | 0.5.sp | 16.sp | Section labels |
| labelSmall | 11.sp | W600 | 0.5.sp | 16.sp | Chips |

### Shape-система

| Токен | Значение | Применение |
|-------|----------|------------|
| ShapeKeyTokens.CornerExtraSmall | 4.dp | — |
| ShapeKeyTokens.CornerSmall | 8.dp | Chips |
| ShapeKeyTokens.CornerMedium | 16.dp | TextField, Dropdown |
| ShapeKeyTokens.CornerLarge | 24.dp | PlaceCard, Dialogs |
| ShapeKeyTokens.CornerExtraLarge | 32.dp | FAB, hero-секции |
| ShapeKeyTokens.CornerFull | 50.dp | Кнопки, CircleButton |

В MaterialTheme.shapes передать:
```
shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
```

### Расширенные токены дизайна

Создать файл `ui/theme/LocalGuideDesignTokens.kt` — object с дополнительными токенами, которые нельзя выразить через MaterialTheme:

```kotlin
object LocalGuideDesignTokens {
    // Градиенты (для hero-секций и карточек)
    val heroGradientColors = listOf(Color(0xFFE8622A), Color(0xFFC0410D))
    val cardGradientLight = listOf(Color(0x00000000), Color(0x99000000))

    // Elevation / тени
    val cardElevation = 2.dp
    val fabElevation = 6.dp

    // Цвета категорий (акцент на карточке)
    val categoryColorRestaurant = Color(0xFFD32F2F)  // красный
    val categoryColorCafe       = Color(0xFF795548)  // коричневый
    val categoryColorPark       = Color(0xFF388E3C)  // зелёный
    val categoryColorMuseum     = Color(0xFF1565C0)  // синий
    val categoryColorShop       = Color(0xFFE65100)  // оранжевый
    val categoryColorLandmark   = Color(0xFF6A1B9A)  // фиолетовый
    val categoryColorOther      = Color(0xFF546E7A)  // серо-синий

    // Анимации (длительности)
    val animationDurationShort = 200
    val animationDurationMedium = 350
    val animationDurationLong = 500

    // Spacing
    val screenPaddingHorizontal = 16.dp
    val cardCornerRadius = 24.dp
    val heroSectionHeight = 200.dp
}
```

---

## Предусловия

- [ ] Нет внешних зависимостей (первая задача спринта)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|--------------------|
| `ui/theme/Color.kt` | modify | Заменить Purple80/PurpleGrey80/Pink80 на полные Light и Dark палитры согласно таблицам выше |
| `ui/theme/Theme.kt` | modify | Отключить dynamicColor (установить `dynamicColor = false` по умолчанию); заполнить `LightColorScheme` и `DarkColorScheme` всеми ролями; добавить `shapes = LocalGuideShapes` в MaterialTheme; переименовать функцию в `LocalGuidePlannerTheme` (snake_case в имени убрать) |
| `ui/theme/Type.kt` | modify | Заменить заглушку на полную шкалу типографики согласно таблице выше |
| `ui/theme/LocalGuideDesignTokens.kt` | create | Новый файл с расширенными токенами (градиенты, цвета категорий, elevation, тайминги анимаций) |
| `ui/theme/Shape.kt` | create | Новый файл `val LocalGuideShapes = Shapes(...)` |

### Архитектурные решения

- `LocalGuidePlannerTheme` — единственный ThemeComposable для всего приложения. Все `@Preview` во всех экранах оборачиваются в него.
- `dynamicColor` по умолчанию `false` — брендовая палитра должна работать одинаково на всех устройствах.
- `LocalGuideDesignTokens` — `object` (не `@Composable`), чтобы использоваться как в Composable, так и в обычном Kotlin-коде.
- Цвета категорий (`categoryColorRestaurant` и т.д.) хранятся в токенах, а не рассеяны по экранам. PlaceCategoryExt.kt будет расширен в TASK-011 — в рамках TASK-009 только определить токены.
- Все `Color`-значения в `Color.kt` — только в виде констант `val XxxYyy = Color(0xFFxxxxxx)`. Никаких inline hex в других файлах темы.

### Обновление @Preview

После изменения имени функции `LocalGuidePlannerTheme` (убрать underscore) — обновить все `@Preview` во всех экранах:
- `OnboardingScreen.kt` — 4 Preview
- `PlacesListScreen.kt` — 4 Preview
- `PlaceDetailScreen.kt` — 3 Preview
- `AddEditPlaceScreen.kt` — 2 Preview
- `MainScreen.kt` — 1 Preview

## Критерии приёма (Definition of Done)

- [ ] `Color.kt` содержит полные Light и Dark палитры (все роли из таблиц)
- [ ] `Theme.kt` передаёт все роли в `lightColorScheme` и `darkColorScheme`, `shapes`, `typography`
- [ ] `dynamicColor` по умолчанию `false`
- [ ] `Type.kt` содержит все 15 стилей типографики из таблицы
- [ ] `Shape.kt` создан с `LocalGuideShapes` (5 уровней скругления)
- [ ] `LocalGuideDesignTokens.kt` создан с градиентами, цветами категорий, elevation, тайминги анимаций
- [ ] Все `@Preview` во всех экранах компилируются и отображают новую цветовую схему
- [ ] Приложение компилируется без ошибок
- [ ] Нет hardcoded hex-цветов вне `Color.kt`
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Визуальная проверка Light-темы:**
   - Запустить приложение на устройстве/эмуляторе с Android < 12 (или Android 12+ с отключённым Material You).
   - Ожидаемый результат: кнопки, AppBar, FAB, Chip отображаются в терракотово-оранжевой гамме (primary `#C0410D`), а не фиолетовой.

2. **Визуальная проверка Dark-темы:**
   - Переключить устройство в тёмный режим.
   - Ожидаемый результат: фон тёмный тёплый (`#201A18`), primary светло-лососевый (`#FFB59D`).

3. **Проверка на Android 12+ с Material You:**
   - Запустить на Android 12+.
   - Ожидаемый результат: используется брендовая палитра (не системная), т.к. `dynamicColor = false`.

4. **Компиляция Preview:**
   - Открыть все экраны в Android Studio, проверить Preview.
   - Ожидаемый результат: все Preview рендерятся без ошибок.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-009-design-system
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-15 | todo | Tech Director | Задача создана |
| 2026-03-15 | review | Developer | Реализована дизайн-система: Color.kt, Theme.kt, Type.kt, Shape.kt, LocalGuideDesignTokens.kt; все Preview обновлены |
