# TASK-003b: Instrumented DAO-тесты с in-memory Room

**Автор:** Tech Director
**Дата создания:** 2026-03-14
**Тип:** feature
**Приоритет:** High
**Спринт:** SPRINT-1
**Зависимости:** TASK-003

---

## Описание

Реализовать `PlaceDaoTest` в `src/androidTest/` с использованием `Room.inMemoryDatabaseBuilder`. Цель — верифицировать реальное SQL-поведение Room: семантику `@Upsert` при конфликте первичного ключа, реактивность `Flow<List<PlaceEntity>>` при изменениях данных, корректность SQL-запросов (фильтрация, сортировка по `createdAt DESC`).

Эта задача закрывает разрыв, возникший при разделении тест-ответственности в TASK-003: `PlacesRepositoryImplTest` (MockK, `src/test/`) проверяет логику делегирования; `PlaceDaoTest` (in-memory Room, `src/androidTest/`) проверяет корректность SQL и Room-контрактов.

## Архитектурное решение

Тестирование DAO через `Room.inMemoryDatabaseBuilder` требует Android-контекста (`Context`). Это принципиальное ограничение Android SDK — Room создаёт SQLite-базу данных в памяти через нативный драйвер, который недоступен в чистой JVM. Поэтому:

- Роболектрик **не применяется** — добавляет >100 MB зависимость, имеет известные проблемы совместимости с AGP 9.x + Kotlin 2.0.21 + KSP + Hilt, значительно замедляет CI.
- Тест размещается в `src/androidTest/` — там доступен настоящий Android-контекст через `ApplicationProvider.getApplicationContext()`.
- `room-testing` находится в `androidTestImplementation` (правильная конфигурация для этого пути).
- Для `./gradlew test` (JVM CI) эти тесты не запускаются — они запускаются отдельно через `./gradlew connectedAndroidTest` при наличии эмулятора.

## Предусловия

- [ ] TASK-003 завершена: `PlaceDao`, `AppDatabase`, `PlaceEntity` созданы
- [ ] В `build.gradle.kts` зависимость `room-testing` находится в `androidTestImplementation`
- [ ] В `build.gradle.kts` зависимость `kotlinx-coroutines-test` добавлена в `androidTestImplementation`

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание |
|------|-----|---------|
| `src/androidTest/java/com/example/localguide_planner/data/local/db/place/PlaceDaoTest.kt` | create | Instrumented тесты для PlaceDao с in-memory Room |
| `android core/app/build.gradle.kts` | modify | Перенести `room-testing` из `testImplementation` в `androidTestImplementation`; добавить `coroutines-test` в `androidTestImplementation` |

### Архитектурные решения

- `AppDatabase` открывается в `@Before` через `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).allowMainThreadQueries().build()`.
- `allowMainThreadQueries()` допустимо только в тестах — снимает запрет на работу с БД в главном потоке, что упрощает тест без `Dispatchers.IO`.
- `AppDatabase` закрывается в `@After` через `db.close()` для освобождения ресурсов между тестами.
- Тест `getAllPlaces` с `Flow` должен использовать `flow.first()` внутри `runBlocking` или `runTest` из `kotlinx-coroutines-test`.
- Каждый тест стартует с чистой БД (in-memory база пересоздаётся в `@Before`).

### Сценарии для покрытия

Сценарии соответствуют тест-сценариям TASK-003:

**1. getAllPlaces — пустая база**
- Получить `Flow` из пустой базы.
- Ожидаемый результат: `emptyList<PlaceEntity>()`.

**2. upsertPlace — вставка новой записи**
- Вставить `PlaceEntity` через `upsertPlace`.
- Вызвать `getAllPlaces().first()`.
- Ожидаемый результат: список содержит одну запись с корректными полями.

**3. upsert существующего места — конфликт ключей (тест-сценарий TASK-003 п.3)**
- Вставить место с `id = "1"` и `name = "Old Name"`.
- Вставить место с тем же `id = "1"` и `name = "New Name"`.
- Вызвать `getAllPlaces().first()`.
- Ожидаемый результат: список содержит одну запись, `name == "New Name"` — `@Upsert` заменил, а не дублировал.

**4. getPlaceById — запись найдена**
- Вставить `PlaceEntity` с `id = "42"`.
- Вызвать `getPlaceById("42")`.
- Ожидаемый результат: возвращён объект с `id == "42"`.

**5. getPlaceById — запись не найдена**
- Вызвать `getPlaceById("not-exist")` на пустой базе.
- Ожидаемый результат: возвращён `null`.

**6. deletePlaceById — удаление существующей записи**
- Вставить две записи с разными `id`.
- Удалить одну через `deletePlaceById`.
- Вызвать `getAllPlaces().first()`.
- Ожидаемый результат: список содержит одну оставшуюся запись.

**7. getAllPlaces — сортировка по createdAt DESC**
- Вставить три записи с `createdAt` 100, 300, 200 (в таком порядке).
- Вызвать `getAllPlaces().first()`.
- Ожидаемый результат: порядок записей в списке — 300, 200, 100.

### Конфигурация build.gradle.kts

```kotlin
// Переместить из testImplementation:
// testImplementation(libs.room.testing)  // УДАЛИТЬ

// Добавить/оставить в androidTestImplementation:
androidTestImplementation(libs.room.testing)
androidTestImplementation(libs.coroutines.test)
```

### Структура теста (псевдокод)

```kotlin
// src/androidTest/.../data/local/db/place/PlaceDaoTest.kt
@RunWith(AndroidJUnit4::class)
class PlaceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PlaceDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.placeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // 7 тест-методов по сценариям выше
}
```

## Критерии приёма (Definition of Done)

- [ ] `PlaceDaoTest.kt` создан в `src/androidTest/`
- [ ] Все 7 сценариев реализованы как отдельные `@Test`-методы
- [ ] База данных создаётся через `Room.inMemoryDatabaseBuilder` (не MockK)
- [ ] В `@Before` — открытие БД, в `@After` — `db.close()`
- [ ] Тест-сценарий для `@Upsert` конфликта ключей присутствует и верифицирует `name` после второго upsert
- [ ] Тест-сценарий для сортировки по `createdAt DESC` присутствует
- [ ] `room-testing` в `build.gradle.kts` перенесена из `testImplementation` в `androidTestImplementation`
- [ ] `coroutines-test` добавлена в `androidTestImplementation` (если ещё не добавлена)
- [ ] `./gradlew assembleDebug` проходит без ошибок
- [ ] `./gradlew test` продолжает проходить (JVM-тесты не затронуты)
- [ ] KTLint без ошибок

## Тест-сценарии для QA

1. **Счастливый путь — upsert и чтение:**
   - Вставить место, прочитать через `getAllPlaces()`.
   - Ожидаемый результат: данные совпадают с вставленными.

2. **Граничный случай — upsert при конфликте ключей:**
   - Вставить место с `id = "X"`, затем вставить другое место с `id = "X"` и изменённым именем.
   - Ожидаемый результат: в базе одна запись, имя обновилось.

3. **Граничный случай — сортировка:**
   - Вставить три записи с разными `createdAt`.
   - Ожидаемый результат: `getAllPlaces()` возвращает записи от новой к старой.

4. **Негативный сценарий — getPlaceById на несуществующем id:**
   - Ожидаемый результат: `null`, без исключения.

---

## Статус выполнения

**Статус:** review

**Ветка:** feature/TASK-003b-place-dao-test
**PR:** (заполняет Developer)
**QA вердикт:** pending
**Мерж:** ожидает

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| 2026-03-14 | todo | Tech Director | Задача создана как результат архитектурного решения по TASK-003 (эскалация от Orchestrator). Разделяет ответственность: репозиторий → MockK (TASK-003), DAO → in-memory Room (эта задача). |
| 2026-03-15 | review | Developer | Реализован PlaceDaoTest (8 тестов), исправлен BUG-018 (PlaceCategoryExt.kt), обновлён build.gradle.kts. |
