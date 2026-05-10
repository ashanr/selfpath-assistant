# Developer Setup & Deployment Guide

Complete guide for setting up, configuring, and deploying the SelfPath Assistant Android app.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Clone & Open](#2-clone--open)
3. [Environment Configuration](#3-environment-configuration)
   - [Local properties](#local-properties-localproperties)
   - [Keystore properties](#keystore-properties-for-release-builds)
   - [API credentials](#api-credentials-settings-ui)
4. [AI Model Setup](#4-ai-model-setup)
   - [Option A — On-device (built-in)](#option-a--on-device-built-in)
   - [Option B — External API](#option-b--external-api)
5. [Build](#5-build)
6. [Run on Device / Emulator](#6-run-on-device--emulator)
7. [Release Deployment](#7-release-deployment)
8. [Project Structure Reference](#8-project-structure-reference)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Prerequisites

| Tool | Required version | Notes |
|------|-----------------|-------|
| **Android Studio** | Ladybug (2024.2) or newer | Meerkat+ recommended |
| **JDK** | 17 | Bundled with Android Studio |
| **Android SDK** | API 35 (compile), API 26 min | Install via SDK Manager |
| **NDK** | Latest stable | Required by MediaPipe (arm64-v8a) |
| **Git** | Any recent version | |
| **Gradle** | Wrapper included (`gradlew`) | Do not install separately |
| **Python + `hf` CLI** | Only if downloading model via CLI | `pip install huggingface_hub` |

> Android Studio manages JDK, SDK, and NDK automatically via **SDK Manager → SDK Tools**.

---

## 2. Clone & Open

```bash
git clone https://github.com/ashanr/selfpath-assistant.git
cd selfpath-assistant
```

Open in Android Studio:

```
File → Open → select the selfpath-assistant folder
```

Gradle sync will run automatically. Wait for it to finish before building.

---

## 3. Environment Configuration

### `local.properties`

Android Studio generates this file automatically at the project root. It is git-ignored. Verify it contains your SDK path:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

**Do not commit `local.properties`.**

---

### Keystore properties (for release builds)

Create `keystore.properties` at the **project root** (git-ignored):

```properties
storeFile=../liberty-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=liberty
keyPassword=YOUR_KEY_PASSWORD
```

Generate the keystore once if you don't have one:

```bash
keytool -genkey -v \
  -keystore liberty-release.keystore \
  -alias liberty \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Then wire it into `app/build.gradle.kts` under `android { signingConfigs { ... } }`:

```kotlin
signingConfigs {
    create("release") {
        val props = java.util.Properties().apply {
            load(rootProject.file("keystore.properties").inputStream())
        }
        storeFile = rootProject.file(props["storeFile"] as String)
        storePassword = props["storePassword"] as String
        keyAlias     = props["keyAlias"] as String
        keyPassword  = props["keyPassword"] as String
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

**Do not commit `keystore.properties` or `*.keystore`.**

---

### API credentials (Settings UI)

API keys are **never stored in source code or config files**. They are entered at runtime through the in-app Settings screen and persisted encrypted in the device's DataStore.

| Setting | Where to configure | Notes |
|---------|-------------------|-------|
| **Use API toggle** | Settings → AI Backend → Use API | Switch between on-device and API |
| **Provider** | Settings → AI Backend → Provider | OpenAI or OpenRouter |
| **API Key** | Settings → AI Backend → API Key | Stored only on the device |
| **Model name** | Settings → AI Backend → Model | Leave blank for provider default |

**Default models:**

| Provider | Default model | Get a key |
|----------|--------------|-----------|
| OpenAI | `gpt-4o-mini` | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) |
| OpenRouter | `openai/gpt-4o-mini` | [openrouter.ai/keys](https://openrouter.ai/keys) |

No environment variables, `.env` files, or `BuildConfig` fields are used for API keys.

---

## 4. AI Model Setup

The app supports two inference modes. You can use either or both — switching is done in Settings at runtime.

### Option A — On-device (built-in)

The model is bundled into the APK via `assets/models/`. It runs fully offline.

**Step 1: Download a model**

Using the `hf` CLI (recommended):

```bash
# Qwen 0.5B — q8 quantized, ~544 MB (recommended for most devices)
hf download litert-community/Qwen2.5-0.5B-Instruct \
  Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.tflite \
  --local-dir app/src/main/assets/models

# Qwen 0.5B — fp32, ~2 GB (higher quality, needs more RAM)
hf download litert-community/Qwen2.5-0.5B-Instruct \
  Qwen2.5-0.5B-Instruct_multi-prefill-seq_f32_ekv1280.tflite \
  --local-dir app/src/main/assets/models
```

Install the CLI if needed:

```bash
pip install huggingface_hub
```

**Step 2: Place the file**

The file must be in:

```
app/src/main/assets/models/<model-file>.tflite
```

The `InferenceEngine` auto-detects the first `.tflite`, `.bin`, or `.task` file in that directory — no code change needed.

**Step 3: Rebuild the app**

```bash
./gradlew assembleDebug
```

> Model files are git-ignored (`.gitignore` excludes `*.bin`, `*.tflite`, `*.onnx`, `*.task`).  
> Every developer must download the model independently.

**Supported formats:**

| File extension | Format | Notes |
|---------------|--------|-------|
| `.tflite` | LiteRT / TFLite flatbuffer | Primary supported format |
| `.task` | MediaPipe Task bundle | Also supported |
| `.bin` | MediaPipe binary | Legacy Gemma format |

**Device requirements for on-device inference:**

| Spec | Minimum |
|------|---------|
| Android | 8.0 (API 26) |
| RAM | 4 GB |
| CPU | arm64-v8a |
| Storage free | ~600 MB (q8 model) |

---

### Option B — External API

No model download or rebuild needed. Configure in Settings at runtime:

1. Open the app → top-right **Settings** gear
2. Scroll to **AI Backend**
3. Toggle **Use API** on
4. Select **Provider** (OpenAI or OpenRouter)
5. Enter your **API Key**
6. Optionally specify a custom **Model** name

When the API key is set and the toggle is on, `GenerateAIResponseUseCase` routes all requests through `ApiInferenceEngine`, which calls the provider's OpenAI-compatible `/chat/completions` endpoint over HTTPS.

---

## 5. Build

All commands run from the project root.

| Command | Output |
|---------|--------|
| `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| `./gradlew assembleRelease` | `app/build/outputs/apk/release/app-release.apk` |
| `./gradlew installDebug` | Builds and installs debug APK on connected device |
| `./gradlew installRelease` | Builds and installs release APK on connected device |
| `./gradlew test` | Unit tests |
| `./gradlew lint` | Lint checks |
| `./gradlew clean` | Clean build outputs |

On Windows replace `./gradlew` with `gradlew.bat` or just `gradlew`.

Gradle JVM heap is set to **4 GB** in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

Increase if you hit out-of-memory errors during build.

---

## 6. Run on Device / Emulator

### Physical device (recommended)

1. Enable **Developer Options** on the device: Settings → About Phone → tap *Build number* 7 times.
2. Enable **USB Debugging** in Developer Options.
3. Connect via USB. Accept the debugging prompt on the device.
4. In Android Studio select the device from the device dropdown and click **Run** (`Shift+F10`).

Or via terminal:

```bash
./gradlew installDebug
adb shell am start -n com.libertyassistant.debug/.MainActivity
```

### Emulator

Emulators lack the RAM and CPU performance for on-device inference. Use them only when **API mode** is enabled in Settings.

Create an AVD with:
- **API 26** or higher
- **x86_64** or **arm64** system image
- **RAM** ≥ 4 GB (configure in AVD Manager → Show Advanced Settings)

```bash
# List available AVDs
emulator -list-avds

# Start a specific AVD
emulator -avd <avd_name>

# Then install
./gradlew installDebug
```

### ADB useful commands

```bash
# List connected devices
adb devices

# View app logs
adb logcat -s "InferenceEngine" "ApiInferenceEngine" "HomeViewModel"

# Clear app data (resets DataStore preferences including API key)
adb shell pm clear com.libertyassistant.debug

# Uninstall
adb uninstall com.libertyassistant.debug
```

---

## 7. Release Deployment

### Build release APK

Ensure `keystore.properties` is configured (see [Section 3](#keystore-properties-for-release-builds)), then:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Install release APK directly (sideload)

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

The release build has `applicationId = "com.libertyassistant"` (no `.debug` suffix) and has R8 minification and resource shrinking enabled.

### Google Play deployment

1. Generate a signed **AAB** instead of an APK:

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

2. Upload the `.aab` to [Google Play Console](https://play.google.com/console) under the target track (internal / alpha / beta / production).

3. The on-device model file (`assets/models/*.tflite`) is included in the APK/AAB automatically at build time. At ~544 MB for the q8 model, the total APK may exceed Play's 150 MB limit — use **Play Asset Delivery** or require the user to configure API mode for large model distribution.

---

## 8. Project Structure Reference

```
selfpath-assistant/
├── app/src/main/
│   ├── AndroidManifest.xml            — INTERNET permission declared here
│   ├── assets/models/                 — Place .tflite / .bin model files here
│   └── kotlin/com/libertyassistant/
│       ├── ai/
│       │   ├── ApiInferenceEngine.kt  — HTTP client for OpenAI-compatible APIs
│       │   ├── ApiProvider.kt         — OpenAI / OpenRouter enum
│       │   ├── AssistantMode.kt       — Philosophical, Poetic, Grammar, Freedom
│       │   ├── InferenceEngine.kt     — On-device MediaPipe LLM wrapper
│       │   ├── ModelLoader.kt         — Extracts model from assets to cache
│       │   └── PromptBuilder.kt       — Per-mode prompt templates
│       ├── data/
│       │   ├── local/                 — Room database, DAO, entity
│       │   ├── preferences/
│       │   │   ├── UserPreferences.kt            — Prefs data class + DataStore keys
│       │   │   └── UserPreferencesRepository.kt  — Read/write API settings
│       │   └── repository/            — JournalRepositoryImpl
│       ├── di/
│       │   ├── AppModule.kt           — Provides InferenceEngine + DataStore
│       │   └── DatabaseModule.kt      — Provides Room DB + DAO + JournalRepository
│       ├── domain/
│       │   ├── model/                 — AIResponse, JournalEntry
│       │   ├── repository/            — IJournalRepository
│       │   └── usecase/
│       │       ├── GenerateAIResponseUseCase.kt  — Routes to API or on-device
│       │       ├── GetJournalEntriesUseCase.kt
│       │       └── SaveJournalEntryUseCase.kt
│       └── presentation/
│           ├── navigation/            — AppNavigation, Screen sealed class
│           ├── ui/
│           │   ├── component/         — ModeSelector, PromptCard
│           │   ├── screen/
│           │   │   ├── HomeScreen.kt
│           │   │   ├── JournalScreen.kt
│           │   │   └── SettingsScreen.kt  — API toggle, provider, key, model UI
│           │   └── theme/             — Color, Theme, Type
│           └── viewmodel/
│               ├── HomeViewModel.kt
│               ├── JournalViewModel.kt
│               └── SettingsViewModel.kt  — Exposes/saves UserPreferences
├── gradle/libs.versions.toml          — All dependency versions
├── gradle.properties                  — Gradle JVM args, Android flags
├── local.properties                   — SDK path (git-ignored, auto-generated)
├── keystore.properties                — Signing config (git-ignored, manual)
├── CONTRIBUTING.md
├── LICENSE
├── README.md
└── SETUP.md                           — This file
```

---

## 9. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| Gradle sync fails: *SDK location not found* | `local.properties` missing | Open project in Android Studio — it generates it automatically |
| Build error: *NDK not found* | NDK not installed | SDK Manager → SDK Tools → NDK (Side by side) → Install |
| App crashes on launch with MediaPipe error | Model file missing or wrong format | Download `.tflite` and place in `assets/models/`, rebuild |
| "AI model not loaded" shown in app | On-device mode active, no model | Switch to API mode in Settings, or add model and rebuild |
| API returns 401 | Invalid or missing API key | Re-enter API key in Settings → AI Backend |
| API returns 429 | Rate limit exceeded | Wait or upgrade API plan; switch to on-device mode |
| Slow inference on device | Device RAM < 4 GB | Use q8 model (`_q8_ekv1280.tflite`) or switch to API mode |
| `hf` command not found | CLI not installed | `pip install huggingface_hub` |
| Out of memory during Gradle build | JVM heap too small | Increase `org.gradle.jvmargs=-Xmx6144m` in `gradle.properties` |
| ADB device not found | USB debugging off | Enable in Developer Options; try a different USB cable |
| Release APK not signed | `keystore.properties` missing | Follow [Section 3](#keystore-properties-for-release-builds) |
