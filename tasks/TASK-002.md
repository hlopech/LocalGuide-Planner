# TASK-002: Доменный слой — модели и интерфейсы репозиториев

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** Critical
**Спринт:** SPRINT-1
**Зависимости:** TASK-001

---

## Описание

Создать полный доменный слой для Sprint 1: доменные модели (`Place`, `UserProfile`), интерфейсы репозиториев (`PlacesRepository`, `UserProfileRepository`) и все UseCase. Этот слой — чистый Kotlin без Android-зависимостей. Он описывает бизнес-логику приложения независимо от источника данных (Room, DataStore, сеть).

Domain-слой является «сердцем» Clean Architecture: именно к нему обращаются ViewModel, и именно его реализует Data-слой.

## Предусловия

- [ ] TASK-001 выполнена: зависимости подключены, проект собирается

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `domain/model/Place.kt` | create | Доменная модель места + enum PlaceCategory |
| `domain/model/UserProfile.kt` | create | Доменная модель профиля пользователя |
| `domain/repository/PlacesRepository.kt` | create | Интерфейс репозитория мест |
| `domain/repository/UserProfileRepository.kt` | create | Интерфейс репозитория профиля |
| `domain/usecase/place/GetPlacesUseCase.kt` | create | Получить все места |
| `domain/usecase/place/GetPlaceByIdUseCase.kt` | create | Получить место по ID |
| `domain/usecase/place/SavePlaceUseCase.kt` | create | Сохранить (добавить или обновить) место |
| `domain/usecase/place/DeletePlaceUseCase.kt` | create | Удалить место |
| `domain/usecase/profile/GetUserProfileUseCase.kt` | create | Получить профиль пользователя |
| `domain/usecase/profile/SaveUserProfileUseCase.kt` | create | Сохранить профиль пользователя |
| `domain/usecase/profile/IsOnboardingCompletedUseCase.kt` | create | Проверить, завершён ли онбординг |

### Архитектурные решения

- **Нет Android-импортов** в пакете `domain/`. Только `kotlin.*`, `kotlinx.coroutines.*`.
- UseCase реализует паттерн **operator fun invoke**: `operator fun invoke(): Flow<List<Place>>`. Это позволяет вызывать UseCase как функцию: `getPlacesUseCase()`.
- `PlacesRepository` возвращает `Flow<List<Place>>` для реактивных обновлений списка.
- `UserProfileRepository` возвращает `Flow<UserProfile>` для реактивного чтения профиля.
- `SavePlaceUseCase` принимает `Place` — если `id` пустой, генерирует UUID (`java.util.UUID.randomUUID().toString()`).
- `PlaceCategory` — enum, описывает типы мест.

### Сигнатуры интерфейсов

```kotlin
// domain/repository/PlacesRepository.kt
interface PlacesRepository {
    fun getPlaces(): Flow<List<Place>>
    suspend fun getPlaceById(id: String): Place?
    suspend fun savePlace(place: Place)
    suspend fun deletePlace(id: String)
}

// domain/repository/UserProfileRepository.kt
interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun isOnboardingCompleted(): Boolean
}
```

### Сигнатуры UseCase

```kotlin
// GetPlacesUseCase
class GetPlacesUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    operator fun invoke(): Flow<List<Place>> = repository.getPlaces()
}

// GetPlaceByIdUseCase
class GetPlaceByIdUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(id: String): Place? = repository.getPlaceById(id)
}

// SavePlaceUseCase
class SavePlaceUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(place: Place) {
        val placeToSave = if (place.id.isBlank()) {
            place.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis())
        } else place
        repository.savePlace(placeToSave)
    }
}

// DeletePlaceUseCase
class DeletePlaceUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(id: String) = repository.deletePlace(id)
}

// IsOnboardingCompletedUseCase
class IsOnboardingCompletedUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): Boolean = repository.isOnboardingCompleted()
}
```

### Модели данных

```kotlin
// domain/model/Place.kt
data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean,
    val createdAt: Long
)

enum class PlaceCategory {
    RESTAURANT, CAFE, PARK, MUSEUM, SHOP, LANDMARK, OTHER
}

// domain/model/UserProfile.kt
data class UserProfile(
    val name: String,
    val isOnboardingCompleted: Boolean
)
```

## Критерии приёма (Definition of Done)

- [ ] Все файлы созданы в пакете `com.example.localguide_planner.domain`
- [ ] Нет ни одного Android-импорта (`android.*`) в пакете `domain/`
- [ ] Все UseCase используют `@Inject constructor`
- [ ] `SavePlaceUseCase` генерирует UUID, если `place.id` пустой
- [ ] `SavePlaceUseCase` устанавливает `createdAt = System.currentTimeMillis()` для новых мест
- [ ] Unit-тесты для `SavePlaceUseCase` (проверка генерации UUID, логика upsert)
- [ ] Unit-тесты для `IsOnboardingCompletedUseCase`
- [ ] `./gradlew test` проходит без ошибок
- [ ] KTLint без ошибок
- [ ] Нет hardcoded строк

## Тест-сценарии для QA

1. **Счастливый путь — SavePlaceUseCase с новым местом:**
   - Вызвать `SavePlaceUseCase` с `place.id = ""`
   - Ожидаемый результат: репозиторий получает place с непустым UUID и заполненным `createdAt`.

2. **Счастливый путь — SavePlaceUseCase с существующим местом:**
   - Вызвать `SavePlaceUseCase` с `place.id = "existing-id"`, `place.createdAt = 12345L`
   - Ожидаемый результат: репозиторий получает place с `id = "existing-id"` и `createdAt = 12345L` (не перезаписывается).

3. **Граничный случай — GetPlaceByIdUseCase с несуществующим ID:**
   - Вызвать `GetPlaceByIdUseCase("non-existent-id")`
   - Ожидаемый результат: возвращает `null`, без исключения.

---

## Статус выполнения

**Статус:** done

**Ветка:** feature/TASK-002-domain-layer
**PR:** #2 (https://github.com/hlopech/LocalGuide-Planner/pull/2)
**QA вердикт:** PASS
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
| 2026-03-14 | review | Developer | Реализован domain-слой: модели, репозитории, UseCase, unit-тесты |
| 2026-03-14 | done | QA Tester | Все критерии выполнены, вердикт PASS |
