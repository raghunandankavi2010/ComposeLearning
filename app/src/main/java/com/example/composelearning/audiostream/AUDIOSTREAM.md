# Real-time Audio Streaming (WebSocket)

A duplex audio-streaming system that mimics modern AI voice agents: the Android client captures
raw PCM from the mic and streams it, chunk-by-chunk with minimal latency, to a Kotlin **Ktor**
server that writes the live stream to a `.pcm` file.

## Topology
```
AudioRecord ──Flow<ByteArray>──▶ StreamAudioUseCase ──▶ OkHttp WebSocket ══ ws://…/stream ══▶ Ktor (CIO) ──▶ audio-<ts>.pcm
   16kHz/mono/16-bit            (domain orchestration)     (binary frames)                      :audio-stream-server
```

## Server — `:audio-stream-server`
- `embeddedServer(CIO)` on port **8080**, route **`/stream`** (`AudioStreamServer.kt`).
- **Binary** frames → appended to a timestamped `recordings/audio-<ts>.pcm`.
- **Text `"END"`** → graceful close; socket drop/failure handled in `finally` (flush + close).
- Concurrent clients each get their own file.

See **Running it end-to-end** below for how to start it, drive the client, and find the capture.

## Running it end-to-end

### 1. Start the server (dev machine)
Run in its **own terminal** — it runs in the foreground and blocks:
```bash
./gradlew :audio-stream-server:run
```
On startup it prints the port and the **exact output directory**:
```
Audio stream server listening on ws://0.0.0.0:8080/stream
Recordings -> /…/ComposeLearning/audio-stream-server/recordings
```
> If Android Studio doesn't list the task, run a **Gradle sync** first (the module was added recently).

### 2. Launch the client (emulator)
Run the `:app` module, then: home → **App Clones & Real-world → "Real-time Audio Streaming (WebSocket)"** (or search "audio").

> **The record button is a toggle and the *first* tap only handles the mic permission.**
> - **Tap 1** (permission not granted) → shows the system dialog. *Does not start streaming.*
> - **Tap 2** (after granting) → opens the WebSocket and starts streaming.
>
> Confirm the on-screen status reads **`● Streaming — N KB sent`** (icon becomes ⏹). Tap ⏹ to stop —
> that sends `"END"` and finalizes the file. A **red error** status means it couldn't reach the
> server (check you're on the emulator, so the `10.0.2.2` endpoint applies).

### 3. Where is my audio on the server?
Captures land in the **`recordings/` folder *inside the server module*** — the Gradle `run` task's
working directory is the module folder, **not** the repo root:
```
audio-stream-server/recordings/audio-<yyyyMMdd-HHmmss-SSS>.pcm
```
The authoritative path is the `Recordings -> …` line printed at startup. List them with:
```bash
ls -lh audio-stream-server/recordings/
```
**File lifecycle / what to expect in the server log:**
- On connect → `▶ client connected — writing to audio-<ts>.pcm` (the file is created, then grows as
  buffered frames flush).
- On stop / `END` / disconnect → `✔ saved audio-<ts>.pcm — <bytes> (~Ns)` (final flush + close).

If you see **no `▶ client connected` line and an empty `recordings/`**, the client never connected —
almost always the missing **second tap** above (status still says *"Tap to start streaming"*).

### 4. Play back a capture
It's headerless raw PCM, so pass the format explicitly:
```bash
ffplay -f s16le -ar 16000 -ch_layout mono audio-stream-server/recordings/audio-<ts>.pcm
# or convert to WAV:
ffmpeg -f s16le -ar 16000 -ac 1 -i audio-stream-server/recordings/audio-<ts>.pcm out.wav
```

## Client — Clean Architecture (`audiostream/`)
```
audiostream/
├── domain/                       # pure Kotlin, no Android — fully unit-testable
│   ├── AudioRecorder.kt          # interface: fun audioChunks(): Flow<ByteArray>
│   ├── AudioStreamClient.kt      # interface: open/send/finish/close + events: Flow<ConnectionEvent>
│   ├── StreamingState.kt         # Idle / Connecting / Streaming(bytesSent) / Stopped / Error
│   └── StreamAudioUseCase.kt     # channelFlow: open → pipe chunks → finish() on teardown
├── data/
│   ├── AudioRecorderImpl.kt      # AudioRecord → cold Flow on Dispatchers.IO (native, no SDK)
│   └── OkHttpAudioStreamClient.kt# OkHttp WebSocket; binary sends, "END" on finish
├── presentation/
│   ├── AudioStreamContract.kt    # AudioStreamUiState (UDF)
│   ├── AudioStreamViewModel.kt   # holds the streaming Job; StateFlow<AudioStreamUiState>
│   └── AudioStreamScreen.kt      # Material 3 (Route → Screen) + accompanist mic permission
└── di/AudioStreamModule.kt       # Koin module
```

- **UDF / MVVM:** one `StateFlow<AudioStreamUiState>`; a single `onToggleStreaming()` intent.
- **Lifecycle:** the ViewModel keeps one `Job`. Stop / `onCleared()` cancels it → the use case's
  `finally` runs `client.finish()` (sends `"END"`, closes the socket) and the recorder Flow's
  `finally` stops & releases `AudioRecord`. No leaks.
- **Threading:** capture + the blocking `AudioRecord.read` loop run on `Dispatchers.IO`; OkHttp
  enqueues sends on its own dispatcher — the main thread is never blocked.
- **DI (Koin):** `recorder`/`client`/`useCase` are `factory`, `OkHttpClient` is `single`,
  `viewModelOf(::AudioStreamViewModel)`. Started in `ComposeLearningApplication.onCreate()`.

## Testability
Both collaborators are interfaces, so the use case and ViewModel are tested with hand-written
fakes — no Android, no mocking library:
- `FakeAudioRecorder` emits canned chunks (optionally `keepOpen` to mimic a live mic).
- `FakeAudioStreamClient` records `sentChunks` and drives `ConnectionEvent`s.
- `StreamAudioUseCaseTest` — chunks forwarded, `finish()` called, failures mapped to `Error`.
- `AudioStreamViewModelTest` — start opens client; stop/`onCleared` finishes it and reports `Stopped`
  (uses `Dispatchers.setMain(StandardTestDispatcher())`).

## Config
- **Endpoint:** `ws://10.0.2.2:8080/stream` (`AudioStreamModule.SERVER_URL`). `10.0.2.2` is the
  emulator's alias for the host's localhost; on a physical device use your machine's LAN IP.
- **Cleartext:** already permitted to `10.0.2.2`/`localhost`/`127.0.0.1` in
  `res/xml/network_security_config.xml`.
- **Permissions:** `RECORD_AUDIO` (runtime, requested in the Compose layer) + `INTERNET` — both
  already declared in the manifest.

## Dependencies (version catalog)
```toml
# :audio-stream-server
ktor-server-core / ktor-server-cio / ktor-server-websockets, logback-classic
# :app
koin-android, koin-androidx-compose            (DI)
okhttp                                          (WebSocket transport, already present)
kotlinx-coroutines-test                         (testImplementation)
```
No heavy third-party audio SDKs — strictly native `AudioRecord` + standard WebSockets.
