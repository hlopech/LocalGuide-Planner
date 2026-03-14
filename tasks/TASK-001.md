# TASK-001: Настройка зависимостей (Hilt, Room, DataStore, Navigation Compose)

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** chore
**Приоритет:** Critical
**Спринт:** SPRINT-1
**Зависимости:** нет

---

## Описание

Текущий `build.gradle.kts` содержит только базовые Compose-зависимости. Для реализации Sprint 1 необходимо подключить Hilt (DI), Room (локальная БД), DataStore Preferences (профиль пользователя), Navigation Compose (граф экранов), а также тестовые зависимости (MockK, Coroutines Test).

Эта задача — фундамент для всех последующих задач Sprint 1. Без неё невозможно написать ни одну строку production-кода.

Также необходимо создать `LocalGuide_PlannerApplication` с аннотацией `@HiltAndroidApp` и аннотировать `MainActivity` как `@AndroidEntryPoint`.

## Предусловия

- [ ] Проект собирается без ошибок в текущем состоянии (`./gradlew assembleDebug`)

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `android core/gradle/libs.versions.toml` | modify | Добавить версии и aliases для Hilt, Room, DataStore, Navigation, MockK, Coroutines |
| `android core/build.gradle.kts` | modify | Добавить Hilt Gradle plugin в classpath |
| `android core/app/build.gradle.kts` | modify | Подключить все новые зависимости, включить kapt/ksp |
| `app/src/main/java/.../LocalGuide_PlannerApplication.kt` | create | Application-класс с `@HiltAndroidApp` |
| `app/src/main/AndroidManifest.xml` | modify | Зарегистрировать `LocalGuide_PlannerApplication` как android:name |
| `app/src/main/java/.../MainActivity.kt` | modify | Добавить `@AndroidEntryPoint` |

### Архитектурные решения

- **KSP вместо KAPT** для Hilt и Room — KSP работает быстрее и поддерживается на Kotlin 2.x. Необходимо добавить плагин `com.google.devtools.ksp`.
- Версии библиотек должны быть совместимы с AGP 9.0.1 и Kotlin 2.0.21.
- Все версии выносятся в `libs.versions.toml` — без хардкода в `build.gradle.kts`.

### Версии зависимостей (рекомендованные)

```toml
[versions]
hilt = "2.52"
room = "2.7.0"
datastore = "1.1.2"
navigationCompose = "2.9.0"
lifecycleViewmodelCompose = "2.10.0"
coroutines = "1.9.0"
mockk = "1.14.0"
ksp = "2.0.21-1.0.28"

[libraries]
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Lifecycle + ViewModel
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# MockK
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### Итоговые зависимости в app/build.gradle.kts

Добавить следующие блоки:
```kotlin
plugins {
    // ...existing...
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle ViewModel
    implementation(libs.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // Test
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.room.testing)
}
```

## Критерии приёма (Definition of Done)

- [ ] `./gradlew assembleDebug` завершается без ошибок
- [ ] `./gradlew test` завершается без ошибок (нет failed tests)
- [ ] `LocalGuide_PlannerApplication` создан и зарегистрирован в AndroidManifest.xml
- [ ] `MainActivity` аннотирован `@AndroidEntryPoint`
- [ ] Все версии библиотек вынесены в `libs.versions.toml`, нет хардкода
- [ ] KSP подключён и работает (проверить наличие generated-папок после сборки)
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь:** запустить `./gradlew assembleDebug` после применения изменений.
   - Ожидаемый результат: BUILD SUCCESSFUL, APK собран.

2. **Граничный случай:** запустить `./gradlew test`.
   - Ожидаемый результат: BUILD SUCCESSFUL, 0 failed tests.

3. **Негативный сценарий:** попытка запустить приложение на эмуляторе без зарегистрированного Application-класса.
   - Ожидаемый результат: после правильной регистрации в Manifest приложение запускается без крэша.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-001-setup-dependencies
**PR:** — (создаётся)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-14 | in_progress | Developer | Начата реализация |
| 2026-03-14 | review | Developer | assembleDebug BUILD SUCCESSFUL, tests PASSED. Hilt обновлён до 2.59.1 для совместимости с AGP 9.0.1. Добавлен android.disallowKotlinSourceSets=false для совместимости KSP + AGP 9. |
