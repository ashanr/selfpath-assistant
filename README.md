# Liberty Assistant Hub

> A fully offline, on-device personal AI assistant for Android — combining philosophical guidance, motivational tools, and intelligent text rephrasing powered by embedded LLM inference.

![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Features

| Feature | Description |
|---|---|
| **Philosophical Mode** | Deep, layered responses rooted in stoicism, existentialism, and eastern wisdom |
| **Poetic Mode** | Transforms your text into stylized, motivational poetic expression |
| **Grammar Check Mode** | Corrects and rephrases text while preserving your original meaning |
| **Freedom Path Mode** | Personal growth guidance that respects autonomy and authentic self-discovery |
| **Freedom Path Journal** | Save and revisit AI-generated reflections with full offline persistence |
| **Dark / Light Theme** | Material 3 dynamic theming with a freedom-inspired color palette |
| **100% Offline** | No internet connection required — all inference runs on-device |

---

## Architecture

```
MVVM + Clean Architecture
├── presentation/     Compose UI, ViewModels, Navigation
├── domain/           Use cases, domain models, repository interfaces
├── data/             Room database, entities, repository implementations
└── ai/               InferenceEngine, ModelLoader, PromptBuilder
```

**Key libraries:**
- [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3
- [Hilt](https://dagger.dev/hilt/) — dependency injection
- [Room](https://developer.android.com/training/data-storage/room) — local journal persistence
- [MediaPipe LLM Inference](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android) — on-device LLM
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Kotlin Coroutines + Flow](https://kotlinlang.org/docs/coroutines-overview.html)

---

## Requirements

| Requirement | Value |
|---|---|
| Android version | 8.0 (API 26) or higher |
| Device RAM | 4 GB minimum (for Gemma-2B) |
| Storage | ~2 GB free (for model file) |
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/ashanr/selfpath-assistant.git
cd selfpath-assistant
```

### 2. Open in Android Studio

File → Open → select the `selfpath-assistant` folder.  
Android Studio will sync Gradle automatically.

### 3. Download an AI model

The app requires a TFLite-format LLM. Two options are supported:

#### Option A — Gemma 2B (recommended, ~1.5 GB)
1. Visit the [MediaPipe LLM Inference guide](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android).
2. Download `gemma-2b-it-cpu-int4.bin` via the Kaggle link provided.
3. Place it in: `app/src/main/assets/models/`

#### Option B — Qwen 0.5B (lightweight, ~350 MB)
1. Download `qwen-0.5b-cpu-int4.bin` from Hugging Face (MediaPipe format).
2. Place it in: `app/src/main/assets/models/`

> **Important:** Model files are excluded from Git (`.gitignore`). Each developer must download them independently.

The `InferenceEngine` automatically detects the first compatible `.bin` or `.tflite` file in the `models/` folder.

---

## Build & Run

### Run on a device or emulator

```bash
./gradlew installDebug
```

Or use Android Studio's **Run** button (`Shift+F10`).

> **Note:** Emulators have limited RAM. Use a physical device with ≥ 4 GB RAM for AI inference.

### Build a debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## Generate a Release APK

### 1. Create a signing keystore (first time only)

```bash
keytool -genkey -v \
  -keystore liberty-release.keystore \
  -alias liberty \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### 2. Create `keystore.properties` in the project root

```properties
storeFile=../liberty-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=liberty
keyPassword=YOUR_KEY_PASSWORD
```

> **Never commit `keystore.properties` or `*.keystore` to Git.**

### 3. Add signing config to `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            val props = java.util.Properties().apply {
                load(rootProject.file("keystore.properties").inputStream())
            }
            storeFile = rootProject.file(props["storeFile"] as String)
            storePassword = props["storePassword"] as String
            keyAlias = props["keyAlias"] as String
            keyPassword = props["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing config
        }
    }
}
```

### 4. Build the release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

## Project Structure

```
selfpath-assistant/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/models/          ← Place LLM model files here
│       ├── res/
│       │   ├── values/strings.xml
│       │   ├── values/themes.xml
│       │   └── xml/
│       └── kotlin/com/libertyassistant/
│           ├── MainActivity.kt
│           ├── LibertyAssistantApp.kt
│           ├── ai/
│           │   ├── AssistantMode.kt
│           │   ├── InferenceEngine.kt
│           │   ├── ModelLoader.kt
│           │   └── PromptBuilder.kt
│           ├── data/
│           │   ├── local/
│           │   │   ├── database/   AppDatabase, JournalDao
│           │   │   └── entity/     JournalEntry (Room entity)
│           │   └── repository/     JournalRepositoryImpl
│           ├── di/
│           │   ├── AppModule.kt
│           │   └── DatabaseModule.kt
│           ├── domain/
│           │   ├── model/          JournalEntry, AIResponse
│           │   ├── repository/     IJournalRepository
│           │   └── usecase/        Generate, Save, GetEntries
│           └── presentation/
│               ├── navigation/     AppNavigation, Screen
│               ├── ui/
│               │   ├── component/  ModeSelector, PromptCard
│               │   ├── screen/     HomeScreen, JournalScreen, SettingsScreen
│               │   └── theme/      Color, Type, Theme
│               └── viewmodel/      HomeViewModel, JournalViewModel
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on code style, architecture rules, and the pull request process.

---

## License

[MIT License](LICENSE) © 2026 liberty-assistant-hub contributors
AI personal assistant guiding users on their chosen path to freedom through motivational tools and philosophical insights.
