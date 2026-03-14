# TASK-NNN: [Краткое название задачи]

**Автор:** Tech Director
**Дата создания:** YYYY-MM-DD
**Тип:** feature | bugfix | refactor | chore
**Приоритет:** Critical | High | Medium | Low
**Спринт:** SPRINT-N
**Зависимости:** TASK-NNN (или "нет")

---

## Описание

[Подробное описание того, что нужно реализовать. Объясни контекст: зачем это нужно пользователю/приложению.]

## Предусловия

- [ ] [Что должно быть выполнено/настроено до начала работы]
- [ ] [Например: TASK-001 должна быть выполнена]

## Технические требования

### Компоненты для создания/изменения

| Файл | Тип | Описание изменений |
|------|-----|-------------------|
| `ui/feature/FeatureScreen.kt` | create | Новый Compose-экран |
| `ui/feature/FeatureViewModel.kt` | create | ViewModel с StateFlow |
| `domain/usecase/ActionUseCase.kt` | create | UseCase для бизнес-логики |
| `domain/repository/FeatureRepository.kt` | create | Interface репозитория |
| `data/repository/FeatureRepositoryImpl.kt` | create | Реализация репозитория |

### Архитектурные решения

[Опиши конкретные решения: какие паттерны применить, как структурировать данные, какие зависимости использовать]

### API / База данных

[Если нужно — опиши структуру запросов к API или схему Room Entity]

```kotlin
// Пример Room Entity (если нужно)
@Entity(tableName = "table_name")
data class ExampleEntity(
    @PrimaryKey val id: String,
    val name: String
)
```

## Критерии приёма (Definition of Done)

- [ ] [Конкретный, проверяемый критерий 1]
- [ ] [Конкретный, проверяемый критерий 2]
- [ ] Unit-тесты для ViewModel написаны и проходят
- [ ] Unit-тесты для UseCase написаны и проходят
- [ ] KTLint без ошибок
- [ ] Нет hardcoded строк/цветов

## Тест-сценарии для QA

1. **Счастливый путь:** [описание нормального сценария]
   - Ожидаемый результат: ...

2. **Граничный случай:** [пустой список / ошибка сети / null]
   - Ожидаемый результат: ...

3. **Негативный сценарий:** [что происходит при ошибке]
   - Ожидаемый результат: ...

---

## Статус выполнения

**Статус:** todo | in_progress | review | qa_review | done | blocked

**Ветка:** feature/TASK-NNN-название (заполняет Developer)
**PR:** #NNN (заполняет Developer)
**QA вердикт:** pending | PASS | FAIL (заполняет QA Tester)
**Мерж:** ожидает | выполнен (заполняет Orchestrator)

### История статусов
| Дата | Статус | Агент | Примечание |
|------|--------|-------|-----------|
| YYYY-MM-DD | todo | Tech Director | Задача создана |
