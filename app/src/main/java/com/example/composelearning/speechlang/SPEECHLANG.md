# Speak & Detect Language

A single-screen feature that listens to speech, transcribes it with Android's native
`SpeechRecognizer`, then detects the spoken language **on-device** with MediaPipe Tasks Text.

## Architecture (Clean Architecture, MVI)
```
speechlang/
├── data/
│   ├── SpeechRecognizerManager.kt      # lifecycle-safe wrapper around android.speech.SpeechRecognizer
│   └── LanguageDetectionRepository.kt  # MediaPipe LanguageDetector, suspend API on a bg dispatcher
├── domain/model/DetectedLanguage.kt    # pure result model (code, displayName, confidence)
└── presentation/
    ├── SpeechLangContract.kt           # SpeechLangUiState + SpeechStatus + SpeechLangIntent
    ├── SpeechLangViewModel.kt          # IDLE→LISTENING→PROCESSING→SUCCESS/ERROR state machine
    └── SpeechLangScreen.kt             # Material 3 screen (+ @Preview), accompanist mic permission
```

- **State:** one `StateFlow<SpeechLangUiState>`, collected with `collectAsStateWithLifecycle()`.
- **Concurrency:** model init and `detect()` run on `Dispatchers.Default` inside the repository;
  recognizer callbacks arrive on the main thread.
- **Permissions:** `RECORD_AUDIO` via `accompanist-permissions` (requested on first mic tap).
- **No leaks:** `SpeechRecognizer.destroy()` and `LanguageDetector.close()` in `onCleared()`.

## `build.gradle` dependency
Wired through the version catalog (`gradle/libs.versions.toml`):
```toml
mediapipeTasksText = "0.10.20"
mediapipe-tasks-text = { group = "com.google.mediapipe", name = "tasks-text", version.ref = "mediapipeTasksText" }
```
```groovy
// app/build.gradle
implementation(libs.mediapipe.tasks.text)   // == "com.google.mediapipe:tasks-text:0.10.20"

// .tflite must not be compressed so MediaPipe can mmap it
androidResources { noCompress += "tflite" }
```

## Required model asset (manual, one-time)
MediaPipe loads its model from `assets/`. Place the language detector model at:
```
app/src/main/assets/language_detector.tflite
```
Download:
`https://storage.googleapis.com/mediapipe-models/language_detector/language_detector/float32/latest/language_detector.tflite`

If the asset is missing, the screen shows a clear error (via snackbar) instead of crashing.

> The platform `SpeechRecognizer` also needs a `<queries>` entry for `android.speech.RecognitionService`
> (Android 11+ package visibility) — already added to the app manifest.
