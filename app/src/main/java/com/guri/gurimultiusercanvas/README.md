# Guri Multi-User Canvas

## Firebase Setup

This application requires Firebase for real-time data synchronization and authentication. Follow these steps to set up Firebase:

1. **Create a Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" and follow the setup wizard
   - Enable Google Analytics for the project

2. **Register Your Android App**:
   - In Firebase Console, click on your project
   - Click "Add app" and select Android
   - Enter your app's package name: `com.guri.gurimultiusercanvas`
   - Register the app

3. **Download Configuration File**:
   - Download the `google-services.json` file
   - Place it in your app module directory (`app/`)

4. **Add Firebase SDK**:
   - The project is already configured with the necessary Firebase dependencies
   - However, without the `google-services.json` file, it won't connect to your Firebase project

5. **Configure Firebase Services**:
   - **Authentication**: Enable Anonymous Authentication in the Firebase Console
   - **Realtime Database**: Create a database and set up rules
   - **Cloud Firestore**: Create a database and set up rules
   - **Storage**: Set up storage for saving canvas images

## Required Firebase Rules

### Firestore Rules
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

### Realtime Database Rules
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
                    root.child('rooms').child($roomId).child('creatorId').val() == auth.uid
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

### Storage Rules
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

## Features

- Real-time synchronized canvas for collaborative drawing
- User authentication (anonymous or email)
- Room creation and joining via unique codes
- Chat messaging between participants
- Admin controls for room moderation
- Undo/redo functionality
- Canvas saving and exporting

## Architecture

The app follows a clean architecture approach with the following modules:
- **app**: Main application module
- **core:model**: Data models
- **core:common**: Common utilities and interfaces
- **core:ui**: Reusable UI components
- **core:data**: Repository layer
- **core:network**: Firebase service implementations
- **feature:auth**: Authentication feature
- **feature:room**: Room management feature
- **feature:canvas**: Canvas drawing feature 