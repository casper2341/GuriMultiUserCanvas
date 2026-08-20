# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

GuriMultiUserCanvas is a native Android app (Kotlin + Jetpack Compose) for real-time collaborative drawing. Multiple users join a "room" via a code and draw on a shared canvas synced through Firebase (Realtime Database for canvas strokes/cursors/chat, Firestore for users/rooms, Storage for saved images, Auth for anonymous/email sign-in).

The project is early-stage: the module graph, DI wiring, and data models are scaffolded, but only the **User** vertical slice (model → network service → repository → DI binding) is fully implemented end-to-end. `feature:auth`, `feature:canvas`, and `feature:room` currently contain only `build.gradle.kts` + `AndroidManifest.xml` (no source yet). `MainActivity` wires a `NavHost` with placeholder (`TemporaryScreen`) destinations for `create_room` / `join_room`. Room and Canvas repositories/network services are stubbed as commented-out bindings in `DataModule` / `NetworkServiceModule`, ready to uncomment as their `Impl` classes are added.

**Firebase is not fully wired into the build**: there is no `google-services.json` in `app/`, and the `com.google.gms.google-services` Gradle plugin is not applied anywhere (not in `gradle/libs.versions.toml`, not in any `build.gradle.kts`). Firebase calls (`FirebaseAuth.getInstance()`, etc.) exist in code but the app will not initialize Firebase correctly until the plugin is added and a real `google-services.json` is dropped into `app/`. See `app/src/main/java/com/guri/gurimultiusercanvas/README.md` for the full Firebase project setup steps and the required Firestore/Realtime Database/Storage security rules.

## Commands

Windows shell — use `gradlew.bat` (a `gradlew` POSIX script also exists but this repo is developed on Windows).

```
# Build the whole project
gradlew.bat build

# Assemble debug APK only (faster than full build)
gradlew.bat :app:assembleDebug

# Run JVM unit tests (all modules)
gradlew.bat test

# Run JVM unit tests for one module, e.g. core:data
gradlew.bat :core:data:test

# Run a single test class
gradlew.bat :app:testDebugUnitTest --tests "com.guri.gurimultiusercanvas.ExampleUnitTest"

# Run a single test method
gradlew.bat :app:testDebugUnitTest --tests "com.guri.gurimultiusercanvas.ExampleUnitTest.addition_isCorrect"

# Instrumented (device/emulator) tests for a module
gradlew.bat :app:connectedDebugAndroidTest

# Lint
gradlew.bat lint

# Clean
gradlew.bat clean
```

There is no separate JVM-only module — every module (including `core:model`) is an `android-library`/`android-application`, so all builds go through AGP and require an Android SDK.

## Module architecture

Gradle modules, dependency direction flows one way (no cycles):

```
app
├── feature:auth   ─┐
├── feature:canvas  ├─→ core:ui, core:data, core:model, core:common
├── feature:room   ─┘
core:data   → core:network, core:model, core:common
core:network→ core:model, core:common
core:ui     → core:model, core:common
core:common → (no project deps)
core:model  → (no project deps; plain Kotlin data classes only)
```

- **core:model** — pure data classes (`User`, `DrawRoom`, `DrawPath`, `CanvasState`, `CursorPosition`, `ChatMessage`, `DrawAction`) and the app-wide `Result<T>` sealed class (`Success`/`Error`/`Loading`). No Android/Firebase deps beyond core-ktx.
- **core:common** — `AppConstants` (room codes, brush sizes, default colors, Firebase path segment names) and `ResultUtil` (the `Flow<T>.asResult()` and `safeCall {}` helpers used to wrap Firebase calls into `Result`).
- **core:network** — one Firebase-facing service interface + impl per domain (e.g. `UserNetworkService` / `UserNetworkServiceImpl`; `RoomNetworkService` and `CanvasNetworkService` interfaces exist but have no impl yet). Provides `FirebaseAuth`/`FirebaseFirestore`/`FirebaseDatabase`/`FirebaseStorage` singletons via Hilt in `NetworkModule`, and binds service interfaces to impls in `NetworkServiceModule`.
- **core:data** — repository layer (`UserRepository`/`UserRepositoryImpl`) that wraps the corresponding `core:network` service and is what feature modules should depend on directly (never call `core:network` services directly from `feature:*`). Bound via Hilt in `DataModule`. Also has Room (local DB) deps pulled in even though no `@Entity`/`@Dao` exist yet.
- **core:ui** — shared Compose components used across features: `DrawingCanvas`, `ColorSelector`, `BrushSizeSelector`, `DrawingToolSelector`, `UserCursor`.
- **feature:auth / feature:room / feature:canvas** — screen-level Compose UI + ViewModels per feature, each depending on `core:ui`, `core:data`, `core:model`, `core:common`. Not yet implemented.
- **app** — `CanvasApp` (`@HiltAndroidApp`), `MainActivity` with the top-level Compose `NavHost`, and the app theme.

### Conventions established by the User slice (follow when adding Room/Canvas verticals)

- Every async network call returns `Result<T>` (`core.model.Result`), not raw exceptions — wrap Firebase calls in `try/catch` returning `Result.success(...)`/`Result.error(...)`, matching `UserNetworkServiceImpl`.
- Realtime listeners (Firestore snapshot listeners, `FirebaseAuth.AuthStateListener`, RTDB `ValueEventListener`) are exposed as `Flow<Result<T>>` via `callbackFlow { ... awaitClose { ... } }`, not callbacks — see `UserNetworkServiceImpl.observeCurrentUser()`.
- DI is Hilt throughout: `@Module @InstallIn(SingletonComponent::class)`, `@Binds` for interface→impl bindings (`NetworkServiceModule`, `DataModule`), `@Provides` for external SDK instances (`NetworkModule`). Add new bindings by uncommenting the pre-written (but commented-out) `@Binds` stubs in these files rather than restructuring them.
- Firebase collection/path names live in `AppConstants` (`FIREBASE_USERS_PATH`, `FIREBASE_ROOMS_PATH`, etc.) — use these constants instead of string literals when adding new network services.
- Repositories (`core:data`) are thin pass-throughs to network services today; feature ViewModels should depend on repository interfaces, never on `core:network` service interfaces or Firebase types directly.
