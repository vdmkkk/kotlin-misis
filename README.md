# Habits Tracker Android MVP

This repository contains a Kotlin Android habits tracker MVP built to demonstrate:

- clean architecture with `presentation`, `domain`, and `data` layers
- state management with `ViewModel`, `StateFlow`, and one-off UI events
- external integrations through `Room` and `Retrofit`
- concurrency and reactivity with `coroutines` and `Flow`

## MVP Features

- habits list screen
- create habit screen
- mark a habit as completed for today
- local persistence in Room
- manual sync against a mock remote API implemented through Retrofit and OkHttp

## Requirements

- Java 17
- Android SDK
- Android Studio optional, but recommended

## Build the app

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

`app\build\outputs\apk\debug\app-debug.apk`

## Run on an emulator

Available emulators detected on this machine:

- `Pixel_6_Pro_API_33`
- `Pixel_8_Pro_API_Baklava`

Start an emulator if one is not already running:

```powershell
emulator -avd Pixel_6_Pro_API_33
```

Install the app:

```powershell
.\gradlew.bat installDebug
```

Launch it manually if needed:

```powershell
adb shell am start -n com.example.kotlinmisis/.MainActivity
```

Check connected devices:

```powershell
adb devices
```

## Run on a real Android device

1. On the phone, enable developer options by tapping the build number 7 times.
2. Turn on `USB debugging`.
3. Connect the device over USB.
4. Accept the RSA debugging prompt on the phone.
5. Confirm the device is visible:

```powershell
adb devices
```

6. Install the debug build:

```powershell
.\gradlew.bat installDebug
```

## Run from Android Studio

1. Open this folder in Android Studio.
2. Let Gradle sync complete.
3. Choose an emulator or connected device.
4. Press `Run`.

## Architecture Guide

Main packages under `app/src/main/java/com/example/kotlinmisis`:

- `presentation`: activities, adapters, screen state, and ViewModels
- `domain`: entities, repository contract, and use-cases
- `data`: Room database, Retrofit API, mappers, and repository implementation

## Where the requirements are shown

### 1. Clean architecture

- `presentation/habits/list/HabitsViewModel.kt`
- `presentation/habits/create/CreateHabitViewModel.kt`
- `domain/repository/HabitsRepository.kt`
- `domain/usecase/*`
- `data/repository/HabitsRepositoryImpl.kt`

### 2. State management

- `HabitsViewModel` exposes `StateFlow<HabitsUiState>` for the list screen
- `CreateHabitViewModel` manages form state and validation
- `SharedFlow` is used for one-off events like navigation and messages

### 3. External integrations

- Room local storage lives in `data/local`
- Retrofit API and mock network layer live in `data/remote`

### 4. Concurrency and reactivity

- Room DAO exposes `Flow<List<HabitEntity>>`
- ViewModels collect flows in `viewModelScope`
- sync work runs asynchronously with coroutines
- local updates appear immediately, while remote sync is triggered separately

## Notes

- `local.properties` points to the Android SDK path on this machine.
- The mock remote API is implemented with an OkHttp interceptor, so the network layer is fully demonstrable without external credentials.
