# Архитектурные решения — LocalGuide Planner

> Этот файл ведёт **Tech Director**. Обновляется при изменении архитектуры.
> Последнее обновление: 2026-03-14

---

## Обзор архитектуры

```
┌─────────────────────────────────────────┐
│             UI Layer                    │
│  Compose Screens + ViewModels           │
│  (com.example.localguide_planner.ui)    │
└──────────────────┬──────────────────────┘
                   │ StateFlow / Events
┌──────────────────▼──────────────────────┐
│           Domain Layer                  │
│  UseCases + Repository Interfaces       │
│  (com.example.localguide_planner.domain)│
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│             Data Layer                  │
│  RepositoryImpl + Room + DataStore      │
│  (com.example.localguide_planner.data)  │
└─────────────────────────────────────────┘
```

## Принципы

1. **Однонаправленный поток данных (UDF):** Screen → ViewModel (event) → UseCase → Repository → DataSource → StateFlow → Screen (state)
2. **Dependency Rule:** внутренние слои не зависят от внешних. Domain не знает о data/ui.
3. **Инверсия зависимостей:** ViewModel зависит от интерфейса UseCase/Repository, а не от реализации.
4. **Offline-first:** все данные хранятся локально (Room + DataStore). Repository abstraction обеспечивает готовность к будущей синхронизации.

---

## Структура пакетов (Sprint 1)

```
com.example.localguide_planner/
│
├── MainActivity.kt                        # Точка входа, Hilt + NavHost
│
├── ui/
│   ├── theme/                             # Цвета, типографика, тема (уже существует)
│   ├── navigation/
│   │   ├── AppNavGraph.kt                 # NavHost с вложенными графами
│   │   ├── Screen.kt                      # sealed class — маршруты навигации
│   │   └── BottomNavBar.kt                # Composable — нижняя навигация
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── places/
│   │   ├── list/
│   │   │   ├── PlacesListScreen.kt
│   │   │   └── PlacesListViewModel.kt
│   │   ├── detail/
│   │   │   ├── PlaceDetailScreen.kt
│   │   │   └── PlaceDetailViewModel.kt
│   │   └── addedit/
│   │       ├── AddEditPlaceScreen.kt
│   │       └── AddEditPlaceViewModel.kt
│   └── main/
│       └── MainScreen.kt                  # Scaffold + BottomNavBar + NavHost
│
├── domain/
│   ├── model/
│   │   └── Place.kt                       # Доменная модель
│   ├── repository/
│   │   ├── PlacesRepository.kt            # Интерфейс
│   │   └── UserProfileRepository.kt       # Интерфейс
│   └── usecase/
│       ├── place/
│       │   ├── GetPlacesUseCase.kt
│       │   ├── GetPlaceByIdUseCase.kt
│       │   ├── SavePlaceUseCase.kt
│       │   └── DeletePlaceUseCase.kt
│       └── profile/
│           ├── GetUserProfileUseCase.kt
│           ├── SaveUserProfileUseCase.kt
│           └── IsOnboardingCompletedUseCase.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt             # Room database
│   │   │   └── place/
│   │   │       ├── PlaceEntity.kt         # Room entity
│   │   │       ├── PlaceDao.kt            # Room DAO
│   │   │       └── PlaceMapper.kt         # Entity <-> Domain model
│   │   └── datastore/
│   │       └── UserProfileDataStore.kt    # DataStore Preferences
│   └── repository/
│       ├── PlacesRepositoryImpl.kt
│       └── UserProfileRepositoryImpl.kt
│
└── di/
    ├── DatabaseModule.kt                  # Room + DAO bindings
    ├── DataStoreModule.kt                 # DataStore binding
    └── RepositoryModule.kt                # Repository bindings
```

---

## Слои приложения

### UI Layer
- Compose-экраны без бизнес-логики. Только отображение UiState и отправка событий во ViewModel.
- ViewModel хранит `StateFlow<UiState>` и `SharedFlow<UiEffect>` (one-time events: навигация, toast).
- ViewModel **не импортирует** классы из пакетов `data/` или `domain/model/` напрямую — только через UseCase/Repository интерфейсы.

### Domain Layer
- Чистый Kotlin (без Android-зависимостей).
- UseCase: один метод `operator fun invoke(...)`, возвращает `Flow<T>` или `Result<T>`.
- Repository-интерфейсы: описывают контракт данных независимо от источника.
- Доменная модель `Place` — не связана ни с Room, ни с Retrofit.

### Data Layer
- `PlacesRepositoryImpl` реализует `PlacesRepository`, использует `PlaceDao`.
- `UserProfileRepositoryImpl` реализует `UserProfileRepository`, использует `DataStore<Preferences>`.
- `PlaceMapper` — маппинг между `PlaceEntity` (Room) и `Place` (domain).
- Слой готов к расширению: в будущем можно добавить `RemoteDataSource` без изменения domain.

### DI Layer
- Три Hilt-модуля для разделения ответственности:
  - `DatabaseModule` — предоставляет `AppDatabase`, `PlaceDao`.
  - `DataStoreModule` — предоставляет `DataStore<Preferences>`.
  - `RepositoryModule` — привязывает реализации к интерфейсам (`@Binds`).
- `MainActivity` аннотируется `@AndroidEntryPoint`, Application — `@HiltAndroidApp`.

---

## Навигация (Sprint 1)

### Граф экранов

```
App Start
    │
    ├── [First launch] ──► OnboardingScreen
    │                             │
    │                      [Имя введено]
    │                             │
    └── [Already onboarded] ──► MainScreen
                                    │
                        ┌───────────┴───────────┐
                        │                       │
                   PlacesListScreen         [Future tabs]
                        │
              ┌─────────┴──────────┐
              │                    │
        PlaceDetailScreen   AddEditPlaceScreen
                                   │
                             [Edit existing]
                          PlaceDetailScreen
```

### Решения по навигации

- Точка входа: `MainActivity` определяет стартовый экран через `IsOnboardingCompletedUseCase`.
- `NavHost` с двумя корневыми маршрутами: `onboarding` и `main`.
- `MainScreen` содержит вложенный `NavHost` для Bottom Navigation + Scaffold.
- `Screen.kt` — sealed class с объектами-маршрутами (типобезопасные аргументы через route-строки).
- Bottom Navigation Bar: на Sprint 1 активна только вкладка "Places". Остальные вкладки (Collections, Profile) — placeholder для будущих спринтов.

### Маршруты

| Route | Экран | Аргументы |
|-------|-------|-----------|
| `onboarding` | OnboardingScreen | — |
| `main` | MainScreen (Scaffold) | — |
| `places/list` | PlacesListScreen | — |
| `places/detail/{placeId}` | PlaceDetailScreen | placeId: String |
| `places/add` | AddEditPlaceScreen | — |
| `places/edit/{placeId}` | AddEditPlaceScreen | placeId: String |

---

## Модель данных — Place

### Доменная модель (`domain/model/Place.kt`)

```kotlin
data class Place(
    val id: String,             // UUID
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean,
    val createdAt: Long         // epoch millis
)

enum class PlaceCategory {
    RESTAURANT, CAFE, PARK, MUSEUM, SHOP, LANDMARK, OTHER
}
```

### Room Entity (`data/local/db/place/PlaceEntity.kt`)

```kotlin
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,       // PlaceCategory.name
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean,
    val createdAt: Long
)
```

---

## Профиль пользователя (DataStore)

### Доменная модель (`domain/model/UserProfile.kt`)

```kotlin
data class UserProfile(
    val name: String,
    val isOnboardingCompleted: Boolean
)
```

### Ключи DataStore (PreferencesKeys)

| Ключ | Тип | Описание |
|------|-----|---------|
| `user_name` | String | Имя пользователя |
| `onboarding_completed` | Boolean | Флаг завершения онбординга |

---

## Зависимости между компонентами (Sprint 1)

```
OnboardingViewModel
    └── SaveUserProfileUseCase
            └── UserProfileRepository (interface)
                    └── UserProfileRepositoryImpl
                            └── UserProfileDataStore (DataStore)

PlacesListViewModel
    └── GetPlacesUseCase
            └── PlacesRepository (interface)
                    └── PlacesRepositoryImpl
                            └── PlaceDao (Room)

PlaceDetailViewModel
    └── GetPlaceByIdUseCase
    └── DeletePlaceUseCase
            └── PlacesRepository (interface)

AddEditPlaceViewModel
    └── GetPlaceByIdUseCase   (edit mode)
    └── SavePlaceUseCase
            └── PlacesRepository (interface)
```

---

## Решение по Hilt + Navigation

- `MainActivity` — `@AndroidEntryPoint`, запускает NavHost.
- Все ViewModel инжектируются через `hiltViewModel()` в Compose-экранах.
- Навигационные события (переход после онбординга, открытие деталей) передаются через `SharedFlow<UiEffect>` из ViewModel и обрабатываются в `LaunchedEffect` на стороне Screen.

---

## Готовность к будущим расширениям

| Расширение | Что уже заложено |
|------------|-----------------|
| Firebase Auth | `UserProfileRepository` — интерфейс; в будущем добавится `RemoteUserProfileRepositoryImpl` |
| Онлайн-синхронизация | `PlacesRepository` — интерфейс; `PlacesRepositoryImpl` можно переключить на Remote |
| Карты | Поля `latitude`/`longitude` в модели `Place` уже присутствуют |
| Collections | Отдельный домен; `PlaceEntity` расширяется через `collectionId` FK |
| Search | `PlaceDao` расширяется методом `searchPlaces(query: String)` |

---

## Текущие модули (Sprint 1)

| Фича | Статус | Задачи |
|------|--------|--------|
| Настройка зависимостей | Планируется | TASK-001 |
| Доменный слой Places | Планируется | TASK-002 |
| Data-слой (Room + DataStore) | Планируется | TASK-003 |
| Онбординг / локальный профиль | Планируется | TASK-004 |
| Навигация (граф экранов) | Планируется | TASK-005 |
| PlacesScreen (список) | Планируется | TASK-006 |
| PlaceDetailScreen | Планируется | TASK-007 |
| AddEditPlaceScreen | Планируется | TASK-008 |

---

*Документ обновляется по мере проектирования фич.*
