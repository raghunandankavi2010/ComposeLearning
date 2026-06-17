# ComposeLearning 🚀

A comprehensive playground for mastering **Jetpack Compose** — advanced animations, custom layouts, graphics/AGSL shaders, on-device & cloud AI, and modern Android architecture. Every experiment lives in its own package and is reachable from a single searchable in-app gallery.

## 📱 Project Overview

This project is a living gallery of what's possible with Jetpack Compose, from fundamental `Canvas` operations to high-performance AGSL shaders, real-time camera ML, and Indic-language speech. The home screen (`animcompose/HomeScreen.kt`) groups every demo by category and lets you search across the whole catalog (`animcompose/FeatureCatalog.kt`).

---

## 🌟 Key Features & Showcases

### 🎨 Graphics & Shaders
- **AGSL Shaders**: Fragment shaders for Page Curl (Riveo), Fluid Spring, Mesh Gradients, Shimmer, and Film Grain.
- **Custom Canvas**: Deep dives into `DrawScope`, paths, Bézier curves, and coordinate systems.
- **Mesh Gradients**: Smooth, organic color blending using native Compose drawing.

### 🎭 Animations
- **Shared Elements**: Seamless list↔detail transitions using the Compose Shared Element API.
- **Physics-Based**: Spring, decay, and fling for realistic interactions (Tinder cards, Bouncing Ball).
- **Google Calling Animation**: A faithful recreation of the multi-layered Google Dialer animation.
- **Path Morphing**: SVG path interpolation for smooth silhouette transitions.
- **Disintegration**: A Thanos-snap dissolve driven by a position-keyed wavefront over sampled particles.

### 📐 Custom Layouts & UI
- **Apple Activity Rings**: Concentric ring progress with end-cap shadows and animation.
- **iPod Click Wheel**: Angular-delta rotary scrolling.
- **Clear To-Do Pinch**: A 3D "unfold" triggered by pinching list items apart.
- **Time Range Knob**: A circular 24h dial with draggable start/end knobs.
- **Adaptive Layouts**: Multi-pane list-detail / supporting-pane scenarios via `material3-adaptive`.

### 🗣️ Indic-Language AI
- **Sarvam Speech-to-Text**: Records 16 kHz WAV via `AudioRecord`, uploads over Retrofit multipart to Sarvam's `speech-to-text` API (`saaras:v3`), and shows the transcript plus the detected language and confidence.
- **Sarvam Language ID (offline-first)**: Detects Hindi, Marathi, Gujarati, Telugu, Tamil & Kannada. Resolves the script **on-device** with zero network for 4 of 6 languages; only the shared-Devanagari Hindi-vs-Marathi case falls back to the Sarvam `text-lid` cloud API.
- **On-Device Speech & MediaPipe**: `SpeechRecognizer` dictation with dynamic Indic language-pack download, then MediaPipe Tasks text language detection.

### 📷 Camera & ML
- **AR Glasses (Face Mesh)**: CameraX + ML Kit Face Mesh superimposing virtual spectacles in real time.
- **FormGuard — Squat Coach**: 100% offline CameraX + MediaPipe Pose Landmarker with rep counting and form warnings.

### 🧮 DSA Visualizations
- **Unique Path Visualizer**: Step-by-step DFS + backtracking on an obstacle grid.
- **Sort Animations**: 8+ sorting algorithms (Bubble, Quick, Merge, Timsort, …) in real time.

### 🌐 Networking & Auth
- **Protobuf over HTTP**: Protocol Buffers with OkHttp and generated Kotlin classes (see the `:server` demo).
- **RemoteCompose**: Server-driven UI where the desktop server builds a RemoteCompose document and the app plays it.
- **Passkeys (FIDO2)**: Passwordless auth via the Credential Manager API.

---

## 🛠 Tech Stack

| Area | Choice |
| :--- | :--- |
| **Language** | Kotlin 2.4.x |
| **Build** | AGP 9.2.x · Gradle (Groovy DSL) · version catalog (`gradle/libs.versions.toml`) |
| **UI** | Jetpack Compose (BOM `2026.05.x`), Material 3, Material3 Adaptive |
| **Navigation** | Navigation 3 + Navigation Compose |
| **Networking** | Retrofit + OkHttp, Kotlinx Serialization (Retrofit converter), Protocol Buffers |
| **Graphics** | AGSL (Android Graphics Shading Language) |
| **Camera / ML** | CameraX, ML Kit, MediaPipe Tasks |
| **Lifecycle** | ViewModel, `lifecycle-runtime-compose` |
| **Images** | Coil 3 |
| **SDK** | `minSdk 33` · `compileSdk 37` · JDK 17 |

---

## 🚀 Getting Started

1. **Clone**:
   ```bash
   git clone https://github.com/raghu-kavi/ComposeLearning.git
   ```
2. **Open in Android Studio**: a recent Canary/Preview build is recommended for the experimental APIs.
3. **(Optional) Sarvam API key** — required only for the Sarvam Speech-to-Text and Language ID demos. Add to `local.properties` (never committed):
   ```properties
   sarvam.api.key=YOUR_SARVAM_API_KEY
   ```
   It is surfaced as `BuildConfig.SARVAM_API_KEY`. The Language ID demo still works offline for the four unique-script languages without a key.
4. **(Optional) Protobuf / RemoteCompose server** — run the local server first to see those demos:
   ```bash
   ./gradlew :server:run
   ```
5. **Deploy**: build and run the `:app` module.

---

## 📂 Project Structure

- `:app` — the main Android application; one package per showcase under `com.example.composelearning`.
- `:proto-models` — shared Kotlin/Java models generated from `.proto` definitions.
- `:server` — a small Kotlin desktop server for the Protobuf / RemoteCompose demos.

---

## 📖 Deep-Dive Docs

Many features ship a focused write-up (math, architecture, API contract) alongside their code:

| Feature | Doc |
| :--- | :--- |
| Premium Circular Progress | [`progress/PremiumCircularProgress.md`](app/src/main/java/com/example/composelearning/progress/PremiumCircularProgress.md) |
| Wave Loader | [`progress/WAVE_LOADER.md`](app/src/main/java/com/example/composelearning/progress/WAVE_LOADER.md) |
| Disintegration (Thanos snap) | [`disintegration/README.md`](app/src/main/java/com/example/composelearning/disintegration/README.md) |
| Gradient Heart Fill | [`heartfill/README.md`](app/src/main/java/com/example/composelearning/heartfill/README.md) |
| Parallax Onboarding | [`onboarding/README.md`](app/src/main/java/com/example/composelearning/onboarding/README.md) |
| Apple Activity Rings | [`applerings/ActivityRings.md`](app/src/main/java/com/example/composelearning/applerings/ActivityRings.md) |
| Riveo Page Curl (AGSL) | [`riveo/PageCurlMath.md`](app/src/main/java/com/example/composelearning/riveo/PageCurlMath.md) |
| Fan / Arc Carousels | [`pager/FAN_CAROUSEL_MATH.md`](app/src/main/java/com/example/composelearning/pager/FAN_CAROUSEL_MATH.md) · [`pager/ARC_CAROUSEL_MATH.md`](app/src/main/java/com/example/composelearning/pager/ARC_CAROUSEL_MATH.md) |
| Path Morph | [`pathmorph/PathMorph.md`](app/src/main/java/com/example/composelearning/pathmorph/PathMorph.md) |
| Adaptive Layouts | [`adaptive/README.md`](app/src/main/java/com/example/composelearning/adaptive/README.md) |
| FormGuard (MediaPipe) | [`formguard/FORMGUARD.md`](app/src/main/java/com/example/composelearning/formguard/FORMGUARD.md) |
| Speech → Language | [`speechlang/SPEECHLANG.md`](app/src/main/java/com/example/composelearning/speechlang/SPEECHLANG.md) · [`ondevicespeech/ONDEVICESPEECH.md`](app/src/main/java/com/example/composelearning/ondevicespeech/ONDEVICESPEECH.md) |
| Protobuf over HTTP | [`protobufdemo/PROTOBUF.md`](app/src/main/java/com/example/composelearning/protobufdemo/PROTOBUF.md) |
| Passkeys (FIDO2) | [`permissions/PASSKEYS.md`](app/src/main/java/com/example/composelearning/permissions/PASSKEYS.md) |

---

## 📜 License

```
Copyright 2024 Raghunandan Kavi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
