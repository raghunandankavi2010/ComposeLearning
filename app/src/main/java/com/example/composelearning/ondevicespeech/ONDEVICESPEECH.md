# On-Device Speech (Indic)

A lightweight speech-to-text layer that uses **only** the Android platform recognizer — no bundled
Vosk/Whisper binaries — and dynamically downloads the system language pack for the chosen language.
The final transcript is fed into the existing MediaPipe pipeline
(`speechlang.data.LanguageDetectionRepository`) for language detection.

Targets six Indian languages: Hindi `hi-IN`, Tamil `ta-IN`, Telugu `te-IN`, Marathi `mr-IN`,
Gujarati `gu-IN`, Kannada `kn-IN`.

## Architecture
```
ondevicespeech/
├── data/
│   ├── SpeechDownloadManager.kt           # checkRecognitionSupport + triggerModelDownload (suspend-bridged)
│   └── OnDeviceSpeechRecognizerWrapper.kt  # createOnDeviceSpeechRecognizer dictation, leak-safe
└── presentation/
    ├── SpeechContract.kt   # SpeechUiState machine + IndicLocale + SpeechIntent
    ├── SpeechViewModel.kt  # provision → listen → detect orchestration
    └── OnDeviceSpeechScreen.kt  # Material 3 UI (dropdown, download progress, listening ripple)
```

## State machine (`StateFlow<SpeechUiState>`)
`CheckingLanguagePacks → DownloadingPack(locale, progress?) → ReadyToListen → Listening →
Processing → Success(transcribedText, detectedLanguage) | Error(message)`

## Dynamic language provisioning
- `SpeechRecognizer.isOnDeviceRecognitionAvailable()` gates the whole feature.
- `checkRecognitionSupport(intent, executor, callback)` → bridged to a `suspend` function with
  `suspendCancellableCoroutine`; classifies the locale as INSTALLED / DOWNLOADABLE / UNSUPPORTED.
- `triggerModelDownload(...)`:
  - **API 34+** uses the `ModelDownloadListener` overload → real `onProgress`/`onSuccess` updates
    drive `DownloadingPack(progress)`.
  - **API 33** has no listener overload, so the download is fire-and-forget (`DownloadResult.Scheduled`);
    the UI proceeds to `ReadyToListen` and the first listen attempt will surface a clear error if the
    pack is still arriving.

## Notes
- `RECORD_AUDIO` permission via `accompanist-permissions`; `<queries>` for the recognition service
  is already in the manifest.
- Recognizer + provisioning APIs are called on the main thread and released in `onCleared()`.
- Whether a given language is actually downloadable depends on the device's recognition service
  (typically Google's Speech Services / "Speech Recognition & Synthesis").
