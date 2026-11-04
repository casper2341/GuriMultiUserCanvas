# Guri Multi-User Canvas

A native Android app for real-time collaborative drawing. Multiple users join a shared "room" via a room code and draw together on the same canvas, with strokes, cursors, and chat synced live through Firebase.

> **Status:** early-stage / work in progress. The module structure, data models, and DI wiring are in place, but only the user (auth) data slice is fully implemented end-to-end. The auth, room, and canvas *feature* screens are still scaffolding. See [Project status](#project-status) below.

## Features

- Real-time synchronized canvas for collaborative drawing
- User authentication (anonymous or email)
- Room creation and joining via unique codes
- Chat messaging between participants
- Admin controls for room moderation
- Undo/redo functionality
- Canvas saving and exporting

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3) for UI
- **Hilt** for dependency injection
- **Coroutines / Flow** for async and real-time streams
- **Firebase**: Authentication, Firestore, Realtime Database, Storage
- **Navigation Compose** for in-app navigation
- **Coil** for image loading
- **Room** (local persistence — dependency present, not yet used)

## Project structure

The app follows a modular, clean-architecture-style layout:

```
app                 → Application entry point, navigation host, theme
feature/auth        → Sign-in / sign-up UI (scaffolded, not yet implemented)
feature/room        → Room creation / joining UI (scaffolded, not yet implemented)
feature/canvas      → Drawing screen UI (scaffolded, not yet implemented)
core/ui             → Shared Compose components (DrawingCanvas, ColorSelector, BrushSizeSelector, UserCursor, ...)
core/data           → Repository layer, sits between features and core/network
core/network        → Firebase-backed service implementations
core/model          → Plain Kotlin data models (User, DrawRoom, DrawPath, CanvasState, ...) and the Result<T> type
core/common         → Shared constants and utilities
```

Dependencies flow one direction only: `app` → `feature:*` → `core:ui` / `core:data` → `core:network` → `core:model` / `core:common`.

## Getting started

### Prerequisites

- Android Studio (Koala or newer recommended)
- JDK 8+ (project targets Java 8 bytecode)
- An Android device or emulator running API 24+

### Firebase setup

This app requires a Firebase project for authentication, real-time sync, and storage.

1. ~~**Create a Firebase project** at the [Firebase Console](https://console.firebase.google.com/).~~ Done.
2. ~~**Register the Android app** with package name `com.guri.gurimultiusercanvas`.~~ Done.
3. ~~**Download `google-services.json`** and place it in the `app/` module directory.~~ Done.
4. ~~**Apply the Google Services Gradle plugin** — uncomment `alias(libs.plugins.google.services)` in `app/build.gradle.kts`'s `plugins {}` block.~~ Done — `:app:assembleDebug` builds successfully with it applied.
5. **Enable the following in the Firebase Console:**
   - **Authentication** → enable the Anonymous sign-in provider — Done, confirmed enabled
   - **Cloud Firestore** → create a database — Done
   - **Realtime Database** → create a database — Done (`google-services.json` was re-downloaded afterward so it includes `project_info.firebase_url`)
   - **Storage** → set up a bucket for saved canvas images — Done (project upgraded to Blaze plan)

#### Security rules

**Firestore rules:** (Done — published to the console)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    match /rooms/{roomId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null &&
        get(/databases/$(database)/documents/rooms/$(roomId)).data.creatorId == request.auth.uid;

      match /participants/{participantId} {
        allow read: if request.auth != null;
        allow write: if request.auth != null &&
          (request.auth.uid == participantId ||
          get(/databases/$(database)/documents/rooms/$(roomId)).data.creatorId == request.auth.uid);
      }
    }
  }
}
```

**Realtime Database rules:** (Done — published to the console)
```
{
  "rules": {
    "canvas": {
      "$roomId": {
        ".read": "auth != null",
        ".write": "auth != null && (
                    !data.exists() ||
                    !data.child('isLocked').exists() ||
                    data.child('isLocked').val() == false ||
                    data.child('creatorId').val() == auth.uid
                  )"
      }
    },
    "cursors": {
      "$roomId": {
        ".read": "auth != null",
        "$userId": {
          ".write": "auth != null && auth.uid == $userId"
        }
      }
    },
    "chat": {
      "$roomId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

**Storage rules:** (Done — published to the console)
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /canvas/{roomId}/{allImages=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### Build and run

```
# Build the whole project
gradlew.bat build

# Assemble a debug APK
gradlew.bat :app:assembleDebug

# Install and run on a connected device/emulator
gradlew.bat :app:installDebug
```

### Testing

```
# Run all JVM unit tests
gradlew.bat test

# Run unit tests for a single module
gradlew.bat :core:data:test

# Run instrumented tests (requires a connected device/emulator)
gradlew.bat :app:connectedDebugAndroidTest
```

## Project status

- ✅ Module graph, Hilt DI wiring, and shared data models (`core:model`, `core:common`)
- ✅ User authentication data layer (`core:network` → `core:data`) fully implemented
- ⏳ `feature:auth`, `feature:room`, `feature:canvas` screens — not yet implemented (placeholder destinations only)
- ⏳ Room and Canvas network services / repositories — interfaces defined, implementations pending
- ✅ Google Services Gradle plugin applied; `app/google-services.json` in place, RTDB created and rules published
- ✅ Firestore database and Storage bucket created in the Firebase console, all rules published; Blaze plan active with a budget alert set

See `CLAUDE.md` for a deeper architectural breakdown intended for contributors working with AI coding assistants.
