# Архитектурные решения — LocalGuide Planner

> Этот файл ведёт **Tech Director**. Обновляется при изменении архитектуры.

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
│  RepositoryImpl + Room + Retrofit       │
│  (com.example.localguide_planner.data)  │
└─────────────────────────────────────────┘
```

## Принципы

1. **Однонаправленный поток данных (UDF):** Screen → ViewModel (event) → UseCase → Repository → DataSource → StateFlow → Screen (state)
2. **Dependency Rule:** внутренние слои не зависят от внешних. Domain не знает о data/ui.
3. **Инверсия зависимостей:** ViewModel зависит от интерфейса `UseCase`/`Repository`, а не от реализации.

## Текущие модули (планируемые)

| Фича | Статус | Задачи |
|------|--------|--------|
| Базовый каркас | Scaffold | — |
| Список мест (Places) | Планируется | — |
| Детали места | Планируется | — |
| Создание маршрута | Планируется | — |
| Избранное | Планируется | — |
| Онбординг/авторизация | Планируется | — |

---

*Документ обновляется по мере проектирования фич.*
