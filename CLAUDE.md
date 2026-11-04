# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

GuriMultiUserCanvas is a native Android app (Kotlin + Jetpack Compose) for real-time collaborative drawing. Users create or join a "room" via a short code and draw together on a shared canvas, with strokes, live cursors, and chat synced through Firebase (Firestore for users/rooms/participants, Realtime Database for canvas paths/actions/cursors/chat, Storage for saved canvas images, Auth for anonymous sign-in).

As of this pass, the full pipeline is implemented end-to-end and `:app:assembleDebug` builds successfully. See "Outstanding TODOs" below for what still needs a real Firebase project to actually run.

## Commands

Windows shell — use `gradlew.bat`.

**JDK requirement:** AGP 8.5.2 requires JDK 17. If the shell's default `JAVA_HOME` points at a different version (compilation fails with "Android Gradle plugin requires Java 17"), set it explicitly before running Gradle, e.g.:
```
export JAVA_HOME="/c/Users/<you>/.jdks/jbr-17.0.14"   # or wherever a JDK 17 lives on this machine
```

```
# Build the whole project (builds both debug and release variants — slow)
gradlew.bat build

# Assemble debug APK only (faster, what to use for iteration)
gradlew.bat :app:assembleDebug

# Compile-check a single module without a full assemble (fastest feedback loop)
gradlew.bat :core:data:compileDebugKotlin

# Run JVM unit tests (all modules / one module)
gradlew.bat test
gradlew.bat :core:data:test

# Run a single test class / method
gradlew.bat :app:testDebugUnitTest --tests "com.guri.gurimultiusercanvas.ExampleUnitTest"
gradlew.bat :app:testDebugUnitTest --tests "com.guri.gurimultiusercanvas.ExampleUnitTest.addition_isCorrect"

# Instrumented (device/emulator) tests
gradlew.bat :app:connectedDebugAndroidTest

# Lint / clean
gradlew.bat lint
gradlew.bat clean
```

Every module (including `core:model`) is an `android-library`/`android-application`, so all builds go through AGP and require an Android SDK — there's no pure-JVM module.

## Module architecture

```
app
├── feature:auth   ─┐
├── feature:canvas  ├─→ core:ui, core:data, core:model, core:common
├── feature:room   ─┘
core:data   → core:network, core:model, core:common
core:network→ core:model, core:common
core:ui     → core:model, core:common
core:common → core:model
core:model  → (no project deps; plain Kotlin data classes only)
```

- **core:model** — data classes (`User`, `DrawRoom`, `DrawPath`, `CanvasState`, `CursorPosition`, `ChatMessage`, `DrawAction`, `Participant`) and the `Result<T>` sealed class.
- **core:common** — `AppConstants` (room codes, brush sizes, colors, Firebase path segment names) and `ResultUtil` (`Flow<T>.asResult()`, `safeCall {}`).
- **core:network** — Firebase-facing service impls: `UserNetworkServiceImpl` (Firestore), `RoomNetworkServiceImpl` (Firestore + RTDB for the room→canvas lock mirror), `CanvasNetworkServiceImpl` (RTDB + Storage). Hilt DI in `NetworkModule` (Firebase singletons) and `NetworkServiceModule` (interface bindings).
- **core:data** — repository layer (`UserRepository`, `RoomRepository`, `CanvasRepository`), thin pass-throughs to `core:network`, bound in `DataModule`. Feature modules depend on repositories, never on `core:network` directly.
- **core:ui** — shared Compose components: `DrawingCanvas` (branches rendering/gestures per `DrawPath.ToolType`), `ColorSelector`, `BrushSizeSelector`, `DrawingToolSelector`, `UserCursor`, `ParticipantsList`.
- **feature:auth** — `AuthViewModel` + `SignInScreen` (anonymous sign-in only).
- **feature:room** — `create/`, `join/`, `lobby/` packages (ViewModel + Screen each).
- **feature:canvas** — `CanvasViewModel`/`CanvasScreen` (drawing, undo/redo, chat, cursors, save), `chat/ChatPanel`, `export/CanvasImageExporter` (rasterizes `DrawPath` list to a `Bitmap` via `android.graphics.Canvas`, not composable capture — the pinned Compose BOM predates `GraphicsLayer` capture APIs).
- **app** — `CanvasApp` (`@HiltAndroidApp`), `MainActivity` with the full Compose `NavHost` (`sign_in → home → create_room/join_room → room_lobby/{roomId} → canvas/{roomId}`).

### Conventions established across the codebase (follow these when adding to any vertical)

- Every async network call returns `Result<T>` (`core.model.Result`) — wrap Firebase calls in `try/catch` returning `Result.success(...)`/`Result.error(...)`.
- Realtime listeners (Firestore snapshot listeners, RTDB `ValueEventListener`/`ChildEventListener`, `FirebaseAuth.AuthStateListener`) are exposed as `Flow<Result<T>>` via `callbackFlow { ... awaitClose { ... } }`.
- DI is Hilt throughout: `@Binds` for interface→impl bindings, `@Provides` for external SDK instances, all in `SingletonComponent`.
- Firebase collection/path names live in `AppConstants` (`FIREBASE_USERS_PATH`, `FIREBASE_ROOMS_PATH`, etc.) — use these constants instead of string literals.
- ViewModels use a single `MutableStateFlow<XxxUiState>` exposed as `StateFlow`, with one-shot events (navigation, kicked-from-room) modeled as nullable fields in the state that the screen consumes via `LaunchedEffect` and then clears with a `consumeXxx()`/`onNavigatedXxx()` call — not a separate `Channel`/`SharedFlow`.
- Compose `Color` from a stored ARGB `Int`/`Long` is `Color(x)` directly — **never** `Color(x.toULong())` (that resolves to Compose's unrelated 64-bit packed-color constructor and produces wrong colors; this was a real pre-existing bug, fixed everywhere it appeared).
- Modules using icons beyond the small default set (`Icons.Filled.*` core icons) need `implementation(libs.androidx.material.icons.extended)` declared directly in *their own* `build.gradle.kts` — it's an `implementation` dep in `core:ui`, so it does not propagate transitively to feature modules.

## Outstanding TODOs

**Blocking real usage (needs the project owner, not something Claude can do):**
1. ~~Create a Firebase project, register the Android app (package `com.guri.gurimultiusercanvas`), download `google-services.json` into `app/`.~~ Done — `app/google-services.json` is present and the package name matches.
2. ~~Uncomment `alias(libs.plugins.google.services)` in `app/build.gradle.kts`'s `plugins {}` block.~~ Done — plugin is enabled and `:app:assembleDebug` builds successfully with it applied.
3. ~~Enable **Anonymous Auth** in the Firebase console.~~ Done — confirmed enabled.
4. ~~Create the **Firestore** database in the Firebase console and paste the Firestore rules from `README.md`.~~ Done — database created, rules published.
5. ~~Create the **Storage** bucket in the Firebase console and paste the Storage rules from `README.md`.~~ Done — project upgraded to the Blaze plan (required for Storage), bucket `gurimultiusercanvas.firebasestorage.app` created, rules published. A budget alert was set as a spend tripwire.
6. ~~Create the Realtime Database and paste the RTDB rules from `README.md`.~~ Done — `google-services.json` was re-downloaded after creation and now includes `project_info.firebase_url`; rules are published.
7. Real two-device sync testing (rooms, live cursors, chat, canvas persistence) — unblocked now that Auth/Firestore/Storage/RTDB are all live; not yet performed.

**Note:** `app/google-services.json` is currently untracked and not in `.gitignore` — worth deciding whether it should be ignored before it gets committed.

**Deliberately deferred (see the approved plan for full rationale — not oversights):**
- Email/Google sign-in UI (`firebase-ui-auth` dependency stays unused for now).
- Voice chat (`ChatMessage.MessageType.VOICE` unhandled).
- A "my rooms" browsing screen (`RoomRepository.observeUserRooms` implemented but unused by any screen).
- Real vector eraser (current eraser paints over in the background color rather than hit-testing/removing strokes).
- Undo of "Clear Canvas" (irreversible by design — confirmation-dialog gated instead).
- The redo "stale toggle" edge case: undo/redo target the most-recently-touched stroke per user, not a strict linear history, since `undoAction`/`redoAction` take no path/action id in their interface signatures.
- Room-code QR sharing, automatic system chat messages on join/leave/kick, profile-editing UI (`UserRepository.updateUserProfile` unused), local Room persistence/offline support, canvas pan/zoom or multi-page boards, backend cleanup jobs for stale rooms.

**Known pre-existing gaps not yet addressed:**
- `RoomNetworkServiceImpl.createRoom`'s room-code uniqueness check is a query-then-write, not a transaction — a narrow race window exists if two rooms are created with the same generated code at the exact same moment.
- No automated test coverage was added for the new repositories/ViewModels in this pass (only compile-level verification was done).
