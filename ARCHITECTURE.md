# Architecture

This app follows **Clean Architecture** with **SOLID** and **MVVM**.

## Layers

- **UI (Presentation):** Activities, Fragments, ViewModels. Depends only on **domain** (use cases / abstractions).
- **Domain:** Entities, use cases, repository/engine interfaces. **No** dependency on `data` or Android.
- **Data:** Repository implementations, data sources, DTOs, mappers (data ↔ domain). Depends on **domain**.

Dependency rule: **dependencies point inward**. Domain does not import from `data` or `ui`.

## SOLID

- **S:** Use cases and repositories have a single responsibility.
- **O:** New behaviour via new implementations (e.g. new repositories), not by changing existing code.
- **L:** Implementations are substitutable for their interfaces.
- **I:** Repository and notifier interfaces are focused (e.g. `LoginRepository`, `CrashPredictor`).
- **D:** High-level code depends on abstractions: ViewModels get use cases, use cases get repository interfaces; Hilt provides implementations.

## Dependency injection

- **Hilt** provides singletons (repos, use cases, data sources) and ViewModels (`@HiltViewModel`).
- Auth flow (Login, SignUp, ForgotPassword) uses `@AndroidEntryPoint` and `by viewModels()`.
- Dashboard and services still use `AppContainer` for engine/sensor/ML wiring; these can be migrated to Hilt modules later.

## Domain entities vs data models

- **Domain:** `AuthenticatedUser`, `User`, `EmergencyContact` (core), `SensorState`, `DetectionResult`, `DashboardStatus`. Live in `domain/model` or `core/domain/model`.
- **Data:** Same concepts as DTOs in `data/model`; **mappers** in `data/mapper` convert to/from domain so domain never sees data types.

## Testing

- **Use cases:** Unit-tested with mocked repositories (`LoginUseCaseTest`, `SignUpUseCaseTest`, `CrashDetectionUseCaseTest`).
- **ViewModels:** Unit-tested with mocked use cases and `StandardTestDispatcher` (`LoginViewModelTest`).
- **Coroutines:** `kotlinx-coroutines-test` and `runTest` for synchronous tests.

## Coroutines and leaks

- ViewModels use `viewModelScope` for launch; scope is cancelled when ViewModel is cleared.
- `CrashDetectionService` uses a single `serviceScope` and calls `serviceScope.cancel()` and `voiceAlertManager.shutdown()` in `onDestroy()`; receivers are unregistered to avoid leaks.
