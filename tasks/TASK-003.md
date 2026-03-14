# TASK-003: Data-слой — Room БД, DAO, DataStore, RepositoryImpl, Hilt-модули

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** Critical
**Спринт:** SPRINT-1
**Зависимости:** TASK-001, TASK-002

---

## Описание

Реализовать data-слой приложения: Room-база данных с Entity и DAO для хранения мест, DataStore Preferences для хранения профиля пользователя, реализации репозиториев (`PlacesRepositoryImpl`, `UserProfileRepositoryImpl`), маппер между доменными моделями и Room Entity, а также все Hilt-модули для внедрения зависимостей.

Это задача образует «инфраструктурный» слой — всё, что связано с хранением данных.

## Предусловия

- [ ] TASK-001 выполнена: Room, DataStore, Hilt подключены в зависимостях
- [ ] TASK-002 выполнена: доменные модели и интерфейсы репозиториев созданы

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `data/local/db/place/PlaceEntity.kt` | create | Room entity для таблицы places |
| `data/local/db/place/PlaceDao.kt` | create | DAO с CRUD-операциями |
| `data/local/db/place/PlaceMapper.kt` | create | Маппинг PlaceEntity <-> Place |
| `data/local/db/AppDatabase.kt` | create | Room database с @Database |
| `data/local/datastore/UserProfileDataStore.kt` | create | DataStore Preferences для профиля |
| `data/repository/PlacesRepositoryImpl.kt` | create | Реализация PlacesRepository |
| `data/repository/UserProfileRepositoryImpl.kt` | create | Реализация UserProfileRepository |
| `di/DatabaseModule.kt` | create | Hilt-модуль для Room |
| `di/DataStoreModule.kt` | create | Hilt-модуль для DataStore |
| `di/RepositoryModule.kt` | create | Hilt-модуль для Repository bindings |

### Архитектурные решения

- **Room** используется с KSP (а не KAPT) — аннотации обрабатываются KSP-компилятором.
- **PlaceMapper** — object-класс с двумя функциями: `toEntity(domain: Place): PlaceEntity` и `toDomain(entity: PlaceEntity): Place`. Маппинг `category` через `PlaceCategory.valueOf(entity.category)`.
- **PlaceDao** возвращает `Flow<List<PlaceEntity>>` из Room для реактивных обновлений.
- **UserProfileDataStore** — обёртка над `DataStore<Preferences>`, инкапсулирует ключи и операции read/write.
- **PlacesRepositoryImpl** преобразует `Flow<List<PlaceEntity>>` из DAO в `Flow<List<Place>>` через `map { it.map(PlaceMapper::toDomain) }`.
- **RepositoryModule** использует `@Binds` (не `@Provides`) для привязки реализаций к интерфейсам — это эффективнее.
- `AppDatabase.VERSION = 1`, в будущем миграции через `Migration`.

### Схема Room Entity

```kotlin
// data/local/db/place/PlaceEntity.kt
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,        // PlaceCategory.name
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean,
    val createdAt: Long
)
```

### Схема DAO

```kotlin
// data/local/db/place/PlaceDao.kt
@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY createdAt DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: String): PlaceEntity?

    @Upsert
    suspend fun upsertPlace(place: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlaceById(id: String)
}
```

### AppDatabase

```kotlin
// data/local/db/AppDatabase.kt
@Database(entities = [PlaceEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    companion object {
        const val DATABASE_NAME = "localguide_db"
    }
}
```

### DataStore ключи и структура

```kotlin
// data/local/datastore/UserProfileDataStore.kt
class UserProfileDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    fun getUserProfile(): Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[KEY_USER_NAME] ?: "",
            isOnboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = profile.name
            prefs[KEY_ONBOARDING_COMPLETED] = profile.isOnboardingCompleted
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }.first()
    }
}
```

### Hilt-модули

```kotlin
// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun providePlaceDao(db: AppDatabase): PlaceDao = db.placeDao()
}

// di/DataStoreModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_profile") }
        )
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository
}
```

## Критерии приёма (Definition of Done)

- [ ] `AppDatabase` создана с `PlaceEntity`, версия = 1, `exportSchema = true`
- [ ] `PlaceDao` реализует `getAllPlaces(): Flow`, `getPlaceById`, `upsertPlace`, `deletePlaceById`
- [ ] `PlaceMapper` корректно маппит все поля, включая `category` (String <-> enum)
- [ ] `UserProfileDataStore` инкапсулирует ключи DataStore и операции
- [ ] `PlacesRepositoryImpl` реализует все методы `PlacesRepository`
- [ ] `UserProfileRepositoryImpl` реализует все методы `UserProfileRepository`
- [ ] Все три Hilt-модуля созданы и корректно настроены
- [ ] `RepositoryModule` использует `@Binds`, а не `@Provides`
- [ ] Unit-тесты для `PlaceMapper` (roundtrip: domain -> entity -> domain)
- [ ] Unit-тесты для `PlacesRepositoryImpl` с in-memory Room (используя `room-testing`)
- [ ] `./gradlew assembleDebug` и `./gradlew test` проходят без ошибок
- [ ] KTLint без ошибок
- [ ] Нет hardcoded строк (имя БД — константа в companion object)

## Тест-сценарии для QA

1. **Счастливый путь — PlaceMapper roundtrip:**
   - Создать `Place` со всеми заполненными полями, включая `latitude`/`longitude`.
   - `PlaceMapper.toEntity(place)` → `PlaceMapper.toDomain(entity)`.
   - Ожидаемый результат: результирующий `Place` равен исходному.

2. **Граничный случай — PlaceMapper с null-координатами:**
   - Создать `Place` с `latitude = null`, `longitude = null`.
   - Выполнить roundtrip через маппер.
   - Ожидаемый результат: поля остаются `null`, без NPE.

3. **Граничный случай — PlacesRepositoryImpl: upsert существующего места:**
   - Сохранить место, затем сохранить то же место с изменённым именем (тот же `id`).
   - Ожидаемый результат: `getPlaces()` содержит одно место с обновлённым именем.

4. **Негативный сценарий — UserProfileDataStore при первом запуске:**
   - Запросить профиль до записи каких-либо данных.
   - Ожидаемый результат: возвращается `UserProfile(name = "", isOnboardingCompleted = false)`.

---

## Статус выполнения

**Статус:** todo

**Ветка:** feature/TASK-003-data-layer (заполняет Developer)
**PR:** — (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана |
