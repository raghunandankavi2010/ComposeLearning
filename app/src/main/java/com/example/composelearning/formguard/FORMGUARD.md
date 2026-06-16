# FormGuard — On-Device Vision AI Squat Coach

## 1. Executive Summary & Core Value
FormGuard is a **100% offline, privacy-first** fitness companion. It uses the device camera to
analyse squat biomechanics in real time and gives **zero-latency, on-device** feedback to help
prevent injury — no frames ever leave the phone. V1 focuses on the **back/barbell squat**: live
knee-angle tracking, automatic rep counting, and a "knees caving in" (valgus) warning.

This document is the build spec for the `com.example.composelearning.formguard` feature. It follows
the project's existing camera/AI blueprint, `com.example.composelearning.arglasses` (CameraX + ML
Kit Face Mesh, Clean Architecture), swapping the detector for **MediaPipe Pose Landmarker**.

## 2. Technical Architecture & Data Pipeline
Clean Architecture, three layers, dependencies pointing inward (domain has no Android/MediaPipe
types so the maths is unit-testable on a plain JVM):

```
formguard/
├── data/
│   └── FormGuardAnalyzer.kt        # ImageAnalysis.Analyzer → MediaPipe PoseLandmarker (GPU, LIVE_STREAM)
├── domain/
│   ├── model/PoseLandmark.kt       # pure data: PoseLandmark, PoseFrame, PoseLandmarks indices
│   ├── BiometricsCalculator.kt     # pure atan2 joint-angle maths
│   └── SquatFormEvaluator.kt       # pure squat state machine + rep counter + valgus detection
└── presentation/
    ├── PoseUiState.kt              # sealed interface (Initializing / CameraPermissionRequired / Tracking / Error)
    ├── FormGuardViewModel.kt       # owns analyzer, drives evaluator, exposes StateFlow<PoseUiState>
    └── FormGuardScreen.kt          # permissions, CameraX PreviewView, Canvas skeleton overlay, audio cues
```

### A. Data Layer — Ingestion & AI
- **CameraX** `Preview` + `ImageAnalysis`, bound to the composition lifecycle via `DisposableEffect`
  (same as `arglasses/presentation/CameraPreview.kt`). **Rear camera** (`DEFAULT_BACK_CAMERA`) so a
  spotter/tripod can frame the full body.
- `STRATEGY_KEEP_ONLY_LATEST` — always analyse the freshest frame, never queue.
- Analysis runs on a **single-thread executor**, off the main thread.
- **MediaPipe Pose Landmarker** (`tasks-vision`) in **`RunningMode.LIVE_STREAM`** with the
  **GPU delegate**. It maps 33 normalized 3D body landmarks; results arrive on the result-listener
  callback (asynchronous, like ML Kit's `Task`).
- **Target landmarks (squat):** Hips `23/24`, Knees `25/26`, Ankles `27/28`.

### B. Domain Layer — Mathematical Computation
Joint angle at the **knee** vertex from three connected landmarks (Hip → Knee → Ankle), using the
numerically-stable two-argument arctangent:

```
θ = | atan2(y_ankle − y_knee, x_ankle − x_knee) − atan2(y_hip − y_knee, x_hip − x_knee) |
    folded into [0°, 180°]
```

```
      Hip (x1, y1)
       \
        \
         Knee (x2, y2)   ← θ (knee flexion angle)
        /
       /
      Ankle (x3, y3)
```

**Squat state machine** (`SquatFormEvaluator`, pure & testable):
| Phase | Condition (knee angle θ) | Effect |
|-------|--------------------------|--------|
| **Standing** | θ ≥ 160° | If a valid depth was reached → **increment rep**, reset depth flag |
| **Descending** | 100° < θ < 160° | Coaching: "Go deeper" |
| **Bottom (valid depth)** | θ ≤ 100° | Set `reachedDepth` flag |

**Critical failure — Knee Valgus ("knees caving in"):** during the bent phase, if the horizontal
distance between the **knees** shrinks well below the distance between the **ankles**
(`kneeWidth / ankleWidth < 0.60`), raise `isKneesCaving`.

Hysteresis (depth ≤ 100° to arm, stand ≥ 160° to count) prevents double-counting around a single
threshold. A side is only trusted when its hip/knee/ankle landmark `visibility` exceeds `0.5`.

### C. Presentation Layer — Jetpack Compose
- **State:** a single `StateFlow<PoseUiState>` (sealed interface) exposed from the ViewModel,
  collected with `collectAsStateWithLifecycle()`.
- **Per-frame split (project convention):** the ~30 fps raw `PoseFrame` used to draw the skeleton is
  **not** routed through the `StateFlow` (that would recompose the whole screen every frame).
  Following `ArGlassesContract`/`RiveoContract`, it is held as **Compose snapshot state**
  (`FormGuardViewModel.poseFrame`) and read inside the `Canvas` **draw lambda**, so a new frame
  invalidates drawing only. The coarse `PoseUiState.Tracking` (rep count, valgus flag, ~1°-quantised
  knee angle, feedback string) flows through the `StateFlow`, de-duplicated so it only emits when a
  displayed value actually changes.
- **Overlay:** a transparent Compose `Canvas` over the `AndroidView { PreviewView }` draws the
  Hip→Knee→Ankle skeleton (green normally, **red on valgus**), mapped from normalized coordinates by
  a `FILL_CENTER` center-crop mapper that matches `PreviewView`'s scaling.
- **Audio cues:** a `ToneGenerator` (remembered, released on dispose) chirps on each completed rep
  and emits a warning tone when `isKneesCaving` flips true; the feedback string is shown on screen.

## 3. UI State Schema & Models
```kotlin
sealed interface PoseUiState {
    data object Initializing : PoseUiState
    data object CameraPermissionRequired : PoseUiState
    data class Tracking(
        val kneeAngle: Float,
        val repCount: Int,
        val feedbackMessage: String?,
        val isKneesCaving: Boolean,
    ) : PoseUiState
    data class Error(val errorMsg: String) : PoseUiState
}
```

```kotlin
// Domain (no Android / MediaPipe types) — normalized [0,1] coordinates.
data class PoseLandmark(val x: Float, val y: Float, val visibility: Float)
data class PoseFrame(val landmarks: List<PoseLandmark>, val sourceWidth: Int, val sourceHeight: Int)
```

## 4. Production Optimization Guardrails
- **No frame leaks:** `imageProxy.close()` in a `finally` after `detectAsync`. Unlike ML Kit's
  `Task` (where we must close in `addOnCompleteListener`), MediaPipe's `MediaImageBuilder` conversion
  is **synchronous** inside `detectAsync`, so closing once it returns is correct and leak-free.
- **GC overhead in the loop:** zero-copy `MediaImageBuilder` (no per-frame `Bitmap`); a cached
  `ImageProcessingOptions` reused while the rotation is unchanged; the rep/angle maths allocates only
  one small immutable result per frame on the listener thread (not the camera-buffer thread).
- **Hardware acceleration:** `BaseOptions.builder().setDelegate(Delegate.GPU)`.
- **Lifecycle:** camera bound/unbound and the analysis executor shut down in `DisposableEffect`;
  `PoseLandmarker.close()` in `ViewModel.onCleared()`; **application** context (never an Activity)
  held by the analyzer.

## 5. Required Model Asset (manual, one-time)
MediaPipe loads its model from `assets/`. Download the Pose Landmarker model and place it at:

```
app/src/main/assets/pose_landmarker_lite.task
```

Get it from Google's model index:
`https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task`

`app/build.gradle` marks `*.task` as `noCompress` so MediaPipe can mmap it. If the asset is missing,
the feature renders `PoseUiState.Error` with this exact instruction instead of crashing.
