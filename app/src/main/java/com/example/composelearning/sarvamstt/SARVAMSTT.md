# Sarvam Speech-to-Text (language detection)

Records a short audio clip on-device, uploads it to Sarvam's Speech-to-Text REST API, and shows
the **transcript** plus the **detected language** (BCP-47, e.g. `hi-IN`) with a confidence score.

## Architecture (MVVM)
```
sarvamstt/
├── SarvamSttApi.kt           # Retrofit @Multipart interface + api-key Interceptor + DTO
├── SarvamSttClient.kt        # Retrofit + OkHttp (timeouts) + kotlinx.serialization converter
├── WavAudioRecorder.kt       # AudioRecord → 16 kHz mono PCM with a WAV header
├── SpeechToTextViewModel.kt  # Idle→Recording→Uploading→Success/Error state machine
└── SpeechToTextScreen.kt     # Material 3 screen (Route → Screen) + @Preview + mic permission
```

- **State:** one `StateFlow<SpeechUiState>` (`Idle / Recording / Uploading / Success / Error`),
  collected with `collectAsStateWithLifecycle()`. A single `onRecordToggle()` drives the button.
- **Concurrency:** capture runs on a dedicated thread; the upload runs in `viewModelScope` on
  `Dispatchers.IO`. `runCatching { … }` maps every failure onto `SpeechUiState.Error`.
- **Permissions:** `RECORD_AUDIO` via `accompanist-permissions` (requested on first record tap).
- **No leaks:** an in-progress recording is stopped and the temp file deleted in `onCleared()`.

## Why `AudioRecord`, not `MediaRecorder`
Sarvam STT prefers 16 kHz WAV, but `MediaRecorder` cannot emit WAV/PCM (only AMR / AAC-MP4 / 3GP).
`WavAudioRecorder` captures raw 16 kHz mono 16-bit PCM via `AudioRecord` and writes a 44-byte WAV
header (back-filled with the final sizes on stop) — still a standard framework audio tool.

## Verified API contract
```
POST https://api.sarvam.ai/speech-to-text          (multipart/form-data)
header  api-subscription-key: <key>
parts   file=<audio.wav>  model=saaras:v3  mode=transcribe

200 OK  { "request_id", "transcript", "language_code", "language_probability" }
```

> ⚠️ **Gotcha that returns HTTP 404:** the path is `speech-to-text` and the auth header is
> `api-subscription-key`. Sarvam has **no** `v1/speech/transcribe` route and does **not** accept an
> `api-key` header — both are single constants in `SarvamSttApi`. `saaras:v3` + `transcribe` are correct.

## `build.gradle` dependencies
Added to the version catalog (`gradle/libs.versions.toml`):
```toml
retrofit = "3.0.0"
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
```
```groovy
// app/build.gradle
implementation(libs.retrofit)
implementation(libs.retrofit.converter.kotlinx.serialization)
implementation(libs.okhttp)
implementation(libs.kotlinx.serialization.json)
```
Uses **kotlinx.serialization** (not Gson) — the response DTO is `@Serializable`, and the converter
is built with `Json { ignoreUnknownKeys = true }.asConverterFactory(...)`.

## API key
Supplied via `BuildConfig.SARVAM_API_KEY`, read from `local.properties` (never committed):
```properties
sarvam.api.key=YOUR_SARVAM_API_KEY
```
`SarvamSttClient.create()` defaults to `BuildConfig.SARVAM_API_KEY`, so the default ViewModel
picks it up automatically — no constant, no call-site wiring.

## Manifest
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" /> <!-- runtime -->
<uses-permission android:name="android.permission.INTERNET" />     <!-- install-time -->
```
Both already declared.
