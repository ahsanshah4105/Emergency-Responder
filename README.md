# Emergency Responder

Android app for emergency detection (crash, snatch) and SOS alerts with Clean Architecture, SOLID, MVVM, and Hilt.

## Tech stack

- **Kotlin**, **Coroutines**, **Flow**
- **Architecture:** Clean Architecture + MVVM
- **DI:** Hilt
- **UI:** ViewBinding, Navigation
- **Backend:** Firebase Auth, Firestore
- **ML:** TensorFlow Lite (crash detection, audio classification)

## Setup

1. Clone and open in Android Studio.
2. Add `google-services.json` for Firebase (Auth + Firestore).
3. Set `JAVA_HOME` and run:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew test
   ```

## Features

- Auth: email/password and Google sign-in, sign-up, forgot password
- Dashboard: crash detection (sensors + TFLite), snatch guard (accessibility), audio (clap/whistle)
- Emergency contacts: add/remove, SOS blast
- Profile: update name/email

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for layer boundaries, dependency rule, and testing approach.
