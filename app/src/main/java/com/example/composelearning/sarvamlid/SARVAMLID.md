# Sarvam Language ID (offline-first)

Detects the language of typed/pasted text across six Indic languages — **Hindi, Marathi,
Gujarati, Telugu, Tamil, Kannada**. The script is resolved **on-device** with zero network for
four of the six; only the shared-Devanagari Hindi-vs-Marathi case calls the Sarvam cloud.

## Why offline-first
There is **no official Sarvam Android/Edge SDK** — language identification is a cloud REST API.
But four of our six languages have a *unique* Unicode script, so they need no network at all:

| Language | Code | Script | Resolved offline? |
| :--- | :--- | :--- | :--- |
| Gujarati | `gu-IN` | Gujarati (`Gujr`) | ✅ unique |
| Tamil | `ta-IN` | Tamil (`Taml`) | ✅ unique |
| Telugu | `te-IN` | Telugu (`Telu`) | ✅ unique |
| Kannada | `kn-IN` | Kannada (`Knda`) | ✅ unique |
| **Hindi** | `hi-IN` | **Devanagari** (`Deva`) | ⚠️ shared → cloud |
| **Marathi** | `mr-IN` | **Devanagari** (`Deva`) | ⚠️ shared → cloud |

Hindi and Marathi share Devanagari, so script alone can't separate them — that is the **only**
case that hits the network.

## Architecture (MVVM)
```
sarvamlid/
├── LanguageDetection.kt            # IndicLanguage enum + sealed DetectionResult
├── ScriptDetector.kt               # pure, offline Unicode-block detection (no key/network)
├── SarvamLidService.kt             # OkHttp + kotlinx.serialization client for text-lid
├── SarvamLidRepository.kt          # offline-first orchestration + error mapping
├── LanguageDetectionViewModel.kt   # StateFlow UI state + manual ViewModel Factory
└── LanguageDetectionScreen.kt      # Material 3 screen (Route → Screen) + @Preview
```

- **Flow:** `ScriptDetector` runs first. `Resolved` → `ON_DEVICE` success; `Ambiguous`
  (Devanagari) or `Unknown` → `SarvamLidService` cloud lookup.
- **State:** one `StateFlow<LanguageDetectionUiState>`, collected with `collectAsStateWithLifecycle()`.
- **Concurrency:** the network call runs on `Dispatchers.IO`; OkHttp's async API is bridged with
  `suspendCancellableCoroutine` so coroutine cancellation cancels the in-flight `Call`.
- **No Hilt:** `LanguageDetectionViewModel.Factory(apiKey)` builds the service/repository manually.
- **Graceful degradation:** with no API key the on-device path still works; a Devanagari input
  returns a clear `DetectionResult.Failure` instead of crashing.

## Verified API contract
```
POST https://api.sarvam.ai/text-lid
header  api-subscription-key: <key>
body    { "input": "<text, max 1000 chars>" }

200 OK  { "request_id": "...", "language_code": "mr-IN", "script_code": "Deva" }
```
Unmodeled response fields are ignored (`Json { ignoreUnknownKeys = true }`).

## `build.gradle` dependencies
Both already in the version catalog (`gradle/libs.versions.toml`) — no extra setup:
```groovy
implementation(libs.okhttp)                     // com.squareup.okhttp3:okhttp
implementation(libs.kotlinx.serialization.json) // org.jetbrains.kotlinx:kotlinx-serialization-json
```

## API key
Supplied via `BuildConfig.SARVAM_API_KEY`, read from `local.properties` (never committed):
```properties
sarvam.api.key=YOUR_SARVAM_API_KEY
```
The nav entry passes it in: `LanguageDetectionRoute(apiKey = BuildConfig.SARVAM_API_KEY)`.

## Manifest
Needs only `android.permission.INTERNET` (already declared) for the cloud fallback.
