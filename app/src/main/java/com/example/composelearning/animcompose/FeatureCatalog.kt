package com.example.composelearning.animcompose

import androidx.compose.runtime.Immutable
import com.example.composelearning.AnimScreen

/**
 * Top-level groups the home screen is organized into. Every demo belongs to
 * exactly one group; the home screen lists groups and search cuts across them.
 */
enum class FeatureGroup(val title: String, val description: String) {
    FUNDAMENTALS(
        "Animation Fundamentals",
        "Value-based, transition, physics and infinite animations — the building blocks."
    ),
    CANVAS_GRAPHICS(
        "Canvas & Graphics",
        "Custom drawing on Canvas: geometry, dials, logos, gradients and shadows."
    ),
    SHADERS_IMAGES(
        "Shaders & Images (AGSL)",
        "RuntimeShader effects, image filters, blur and image manipulation."
    ),
    CHARTS_VISUALIZERS(
        "Charts & Visualizers",
        "Charting showcases plus algorithm and particle visualizations."
    ),
    GESTURES_TOUCH(
        "Gestures & Touch",
        "Drag, pinch, rotary and sensor-driven interactions."
    ),
    LISTS_LAYOUTS_PAGERS(
        "Lists, Layouts & Pagers",
        "Lazy lists, custom Layouts, carousels and pager experiments."
    ),
    NAVIGATION_TRANSITIONS(
        "Navigation & Transitions",
        "Nav3, shared elements, tab bars, menus and walkthrough overlays."
    ),
    PROGRESS_BUTTONS(
        "Progress & Buttons",
        "Loaders, progress bars and animated button states."
    ),
    TEXT_TYPOGRAPHY(
        "Text & Typography",
        "Marquee, shimmer, squiggly underlines and slider tracks."
    ),
    APP_CLONES(
        "App Clones & Real-world",
        "Recreations of real app UIs and end-to-end feature demos."
    )
}

@Immutable
data class AnimationCategory(
    val title: String,
    val description: String,
    val route: AnimScreen,
    val group: FeatureGroup
)

/** Single source of truth for every demo reachable from the home screen. */
val FeatureCatalog: List<AnimationCategory> = listOf(
    // ── Animation Fundamentals ──────────────────────────────────────────────
    AnimationCategory(
        "Animation Basics",
        "Value-based and infinite animations",
        AnimScreen.AnimationBasics,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Value-Based Animations",
        "Custom types and keyframes",
        AnimScreen.ValueBasedAnimations,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Transition Animations",
        "State-driven animations",
        AnimScreen.TransitionAnimations,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Physics Animations",
        "Spring, decay, fling",
        AnimScreen.PhysicsAnimations,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "April 2026 Updates",
        "New features: Morphing Shapes, PullToRefreshBox, Shared Elements",
        AnimScreen.April2026Features,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Animated Balance Counter",
        "Count-up balance animation from 0 to target",
        AnimScreen.AnimatedBalance,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Bouncing Ball",
        "Vertical bounce animation with screen boundaries",
        AnimScreen.BouncingBall,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Pulsating Circles",
        "Multiple overlapping pulse animations",
        AnimScreen.PulsatingCircles,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Circular Reveal",
        "Expanding circular path reveal (WhatsApp style)",
        AnimScreen.CircularReveal,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Staggered Grid Animation",
        "Grid items appearing with delayed entrance",
        AnimScreen.StaggeredGrid,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Cell Loader Animation",
        "Cell fading animation",
        AnimScreen.SequentialFadeGridScreen,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Recursive Subdivision",
        "Fractal-like grid subdivision with alternating colors.",
        AnimScreen.RecursivePattern,
        FeatureGroup.FUNDAMENTALS
    ),
    AnimationCategory(
        "Promotional Deal Timer",
        "Real-time countdown for marketplace deals with monotonic time, notification sync, and process death resilience.",
        AnimScreen.PromotionalDeal,
        FeatureGroup.FUNDAMENTALS
    ),

    // ── Canvas & Graphics ───────────────────────────────────────────────────
    AnimationCategory(
        "Canvas Basics Hub",
        "Consolidated fundamental drawing concepts: Math, Drawing primitives, Paths, Bitmaps, Canvas State, and Gestures.",
        AnimScreen.CanvasBasicsHub,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Solar System (Kepler)",
        "All 8 planets orbiting on one Canvas with real Kepler T = r^1.5 mechanics — √-compressed orbits, pause and time-warp slider.",
        AnimScreen.SolarSystem,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Gradient Heart Fill",
        "Bézier heart + linear gradient, revealed by a diagonal wavefront sweeping from bottom-left via clipPath. Tap to replay.",
        AnimScreen.GradientHeartFill,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Disintegration (Thanos snap)",
        "Capture any composable's pixels via GraphicsLayer, then dissolve it into drifting dust. A position-keyed wavefront erodes image strips while sampled particles fly off — heavy pixel work runs off the main thread.",
        AnimScreen.Disintegration,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Matrix Digital Rain",
        "Falling glyph columns with glowing near-white heads and exponential phosphor-green tails — per-column depth dimming, glyph flicker and spawn fade-in on native Canvas.",
        AnimScreen.MatrixRain,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Dotted Text",
        "Rasterize text and draw it as a grid of points.",
        AnimScreen.DottedText,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Mesh Gradient",
        "Demo for mesh gradient using compose",
        AnimScreen.MeshGradient,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Analog watch dial",
        "Analog watch dial",
        AnimScreen.AnimatingWatchDial,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Netflix — Shape redraw",
        "Path + clipPath + gradient sweep. Clean geometry, ~120 LOC. Tap to replay.",
        AnimScreen.NetflixLogo,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Netflix — Paint redraw (Anmol port)",
        "Port of @anmolverma's compose-animation-examples: 31 gradient strips + parallel keyframe tracks.",
        AnimScreen.AnmolNetflix,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Shadow Playground",
        "Every Compose shadow: elevation, colored ambient/spot, dropShadow, innerShadow, brush.",
        AnimScreen.ShadowsPlayground,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "SVG Path Morphing",
        "Drag a slider to morph phone silhouettes across eras via per-coordinate SVG path interpolation.",
        AnimScreen.PathMorph,
        FeatureGroup.CANVAS_GRAPHICS
    ),
    AnimationCategory(
        "Bottle Wave Animation",
        "Animating bottle wave filling",
        AnimScreen.BottleWaveAnimation,
        FeatureGroup.CANVAS_GRAPHICS
    ),

    // ── Shaders & Images (AGSL) ─────────────────────────────────────────────
    AnimationCategory(
        "AGSL Shader Demos",
        "Blur, frosted glass, mesh gradient, shimmer, liquid button, film grain",
        AnimScreen.ShaderDemos,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Simple Time Shader",
        "Sine-driven red channel color oscillation (GLSL port)",
        AnimScreen.SimpleTimeShader,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Shader Ripple",
        "Touch-driven distortion wave with aspect correction",
        AnimScreen.ShaderRipple,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Image Processing (AGSL)",
        "Instagram-style filters rendered as an AGSL RuntimeShader RenderEffect.",
        AnimScreen.ImageProcessing,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Riveo — Page Curl (AGSL)",
        "Port of wcandillon's Skia page curl — drag a card to peel the page over a cylinder; springs back on release.",
        AnimScreen.RiveoPageCurl,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Image Cropper",
        "Production cropper: pinch-zoom, resize a rule-of-thirds frame, crop to rectangle/circle/star, save as PNG.",
        AnimScreen.ImageCropper,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "AR Glasses (Face Mesh)",
        "CameraX + ML Kit Face Mesh: tracks your face in real time and superimposes virtual spectacles.",
        AnimScreen.ArGlasses,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "FormGuard — Squat Coach (MediaPipe)",
        "100% offline CameraX + MediaPipe Pose Landmarker: real-time knee-angle tracking, automatic rep counting and a 'knees caving in' warning.",
        AnimScreen.FormGuard,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Crop Doctor — Plant Disease Detector (TFLite)",
        "100% offline on-device TFLite classifier (PlantVillage, 38 classes / 14 crops). Snap or pick a leaf photo and get the likely pest/disease plus plain-language treatment steps for farmers.",
        AnimScreen.CropDoctor,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Photo Quality Check (NIMA)",
        "On-device Google NIMA (technical) TFLite model scores capture quality (blur, noise, exposure) 1–10. Try the bundled samples or score your own photo from the gallery or camera.",
        AnimScreen.PhotoQuality(),
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Speak & Detect Language (MediaPipe)",
        "SpeechRecognizer dictation → MediaPipe Tasks Text language detection. Talk, see the transcript, and get the detected language with confidence.",
        AnimScreen.SpeechLang,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "On-Device Speech (Indic)",
        "On-device SpeechRecognizer for 6 Indian languages with dynamic language-pack download, then MediaPipe language detection on the transcript.",
        AnimScreen.OnDeviceSpeech,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Sarvam Language ID (offline-first)",
        "Detects Hindi, Marathi, Gujarati, Telugu, Tamil & Kannada. Resolves script on-device with zero network; only Hindi-vs-Marathi (shared Devanagari) calls the Sarvam cloud text-lid API.",
        AnimScreen.SarvamLid,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Sarvam Speech-to-Text (language detect)",
        "Records a 16kHz WAV clip via AudioRecord, uploads it to Sarvam STT (saaras:v3) over Retrofit/Gson multipart, and shows the transcript plus the detected language with confidence.",
        AnimScreen.SarvamStt,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Real-time Audio Streaming (WebSocket)",
        "Duplex PCM streaming like AI voice agents: AudioRecord → Coroutine Flow → OkHttp WebSocket → Ktor server writing a .pcm file. Clean Architecture + Koin DI. Run ./gradlew :audio-stream-server:run first.",
        AnimScreen.AudioStream,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Access & Refresh Tokens (Auth flow)",
        "End-to-end JWT-style auth: login → short-lived access token + refresh token → protected /profile call that silently auto-refreshes on 401 via an OkHttp Authenticator. Clean Architecture + Koin + Retrofit, with a live flow log. Run ./gradlew :auth-server:run first.",
        AnimScreen.AuthTokenDemo,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Blur Effects",
        "Modifier.blur and Haze frosted-glass demos",
        AnimScreen.BlurEffects,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Zoomable Image",
        "Pinch-to-zoom and pan with rememberTransformableState — double-tap to reset.",
        AnimScreen.ZoomableImage,
        FeatureGroup.SHADERS_IMAGES
    ),
    AnimationCategory(
        "Overlapping Images",
        "Custom Layout that stacks avatars with a slider-controlled overlap factor",
        AnimScreen.OverlappingImages,
        FeatureGroup.SHADERS_IMAGES
    ),

    // ── Charts & Visualizers ────────────────────────────────────────────────
    AnimationCategory(
        "Charts & Waves Hub",
        "Consolidated charting showcase: Line, Bar, Donut, Pie, Candle, Speedometer, Temperature, and Sine Waves.",
        AnimScreen.ChartsHub,
        FeatureGroup.CHARTS_VISUALIZERS
    ),
    AnimationCategory(
        "Particle Hub",
        "Consolidated particle systems: 3D Explosion, Continuous Stream, Realistic Physics, and Fireworks.",
        AnimScreen.ParticleHub,
        FeatureGroup.CHARTS_VISUALIZERS
    ),
    AnimationCategory(
        "Particle Field (steering / morph)",
        "Port of Flutter's animated_particles: particles sampled from text pixels arrive into the shape, morph between shapes (tap), flee from your finger (drag) and escape off-screen (long-press) — a structure-of-arrays engine drawn in one drawPoints call.",
        AnimScreen.ParticleField,
        FeatureGroup.CHARTS_VISUALIZERS
    ),
    AnimationCategory(
        "Sort Animations",
        "Tabbed hub: bubble, quick, insertion, selection, shell, merge, heap, and Timsort.",
        AnimScreen.SortAnimation,
        FeatureGroup.CHARTS_VISUALIZERS
    ),
    AnimationCategory(
        "Unique Path Visualizer",
        "Visualizes the DFS + Backtracking algorithm for finding unique paths in a grid with obstacles.",
        AnimScreen.UniquePathVisualizer,
        FeatureGroup.CHARTS_VISUALIZERS
    ),

    // ── Gestures & Touch ────────────────────────────────────────────────────
    AnimationCategory(
        "Spinning Wheel",
        "Spinning wheel",
        AnimScreen.SpinningWheel,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Fold Card (pinch)",
        "Pinch vertically to bend a card in half in 3D — two faces fold about the crease with perspective + shading.",
        AnimScreen.FoldCard,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "iPod Click Wheel",
        "Drag around the wheel to scroll/highlight songs; center button selects. Rotary angular-delta scrolling.",
        AnimScreen.IpodWheel,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Clear To-Do (pinch-create)",
        "Pinch two rows apart to unfold a 'Create a new Task' row in the gap; release past threshold to insert.",
        AnimScreen.ClearTodo,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Stacked Tinder Cards",
        "Swipeable cards with interaction physics",
        AnimScreen.StackedCards,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Physics Game",
        "Bubble Pop Game",
        AnimScreen.GameEnvironment,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Draggable Side Sheet",
        "Panel that pulls out from the right side of the screen",
        AnimScreen.DraggableSheet,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Time Range Knob",
        "Circular 24h dial with two draggable knobs — drag to set bedtime and wake-up.",
        AnimScreen.TimeRangeKnob,
        FeatureGroup.GESTURES_TOUCH
    ),
    AnimationCategory(
        "Sensor Reactive Card",
        "Credit card that tilts based on device sensors",
        AnimScreen.SensorCard,
        FeatureGroup.GESTURES_TOUCH
    ),

    // ── Lists, Layouts & Pagers ─────────────────────────────────────────────
    AnimationCategory(
        "Adaptive Layouts (multi-pane)",
        "Four scenarios with material3-adaptive: list-detail pane, supporting pane, adaptive grid, and a WindowSizeClass-driven reflowing detail screen. Built to learn production multi-screen layouts.",
        AnimScreen.AdaptiveLayouts,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Parallax Onboarding",
        "Multi-layer onboarding pager — layers drift at different rates for depth, while the background and pill page indicator interpolate their accent color between pages.",
        AnimScreen.ParallaxOnboarding,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Shorts Video Feed",
        "TikTok-style endless VerticalPager feed: one shared ExoPlayer hopping between SurfaceViews, LRU disk cache pre-buffering the next clip.",
        AnimScreen.ShortsFeed,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Lists Showcase",
        "12 tabbed list demos: alerts, products, sticky, reorder, swipe, staggered, news, circular.",
        AnimScreen.ListsShowcase,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Animated Entry List",
        "LazyColumn rows fade + slide onto position as they appear; first batch cascades in (staggered).",
        AnimScreen.AnimatedListEntry,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Per-item ViewModels (Compose)",
        "Scope a ViewModel to one list item or pager page. LazyColumn + HorizontalPager demo.",
        AnimScreen.PerItemViewModel,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Percentage Layout",
        "BoxWithConstraints + percentage-based offset positioning",
        AnimScreen.PercentageLayout,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Arc List Navigation",
        "Interactive circular layout with drag-to-spin physics",
        AnimScreen.ArcList,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "YouTube Style Screen",
        "Complex layout with custom concave shapes and nested scrolling",
        AnimScreen.YouTubeStyle,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Pager & Carousel Showcase",
        "Tabbed showcase: Instagram coverflow, Instagram v2, HorizontalPager demo.",
        AnimScreen.PagerShowcase,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Top-Right Fan Carousel",
        "Stacked-card carousel — front card centered, others fanned toward the top-right. Drag-to-dismiss.",
        AnimScreen.FanCarousel,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),
    AnimationCategory(
        "Arc Carousel (Swiggy Instamart)",
        "LazyRow + snap fling laid out along a dome curve at the bottom — center item raised and highlighted in a circle.",
        AnimScreen.ArcCarousel,
        FeatureGroup.LISTS_LAYOUTS_PAGERS
    ),

    // ── Navigation & Transitions ────────────────────────────────────────────
    AnimationCategory(
        "Nav3 — Tabs + Shared Elements",
        "Single NavDisplay with per-tab back stacks (Photos / Articles / Profile). Bottom bar hides on detail screens.",
        AnimScreen.TabsSample,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Product Shared Elements",
        "Cinema-style transitions between product list and details",
        AnimScreen.SharedElementProduct,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Chat App Navigation",
        "Shared element transitions in a messaging UI",
        AnimScreen.ChatApp,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Fluid Tab Bar",
        "Morphing indicators and spring-based interactions",
        AnimScreen.FluidTabs,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Circular Menu",
        "Animated menu buttons emerging from center",
        AnimScreen.CircleMenu,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Spotlight Walkthrough",
        "Interactive feature highlighting overlay system",
        AnimScreen.Spotlight,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),
    AnimationCategory(
        "Tutorial Overlay",
        "Coach-mark walkthrough on a LazyColumn — auto-scrolls to off-screen targets.",
        AnimScreen.TutorialOverlay,
        FeatureGroup.NAVIGATION_TRANSITIONS
    ),

    // ── Progress & Buttons ──────────────────────────────────────────────────
    AnimationCategory(
        "SmoothProgressBar (Compose port)",
        "Port of castorflex/SmoothProgressBar — indeterminate horizontal sections sliding with cycling colors.",
        AnimScreen.SmoothProgress,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Multi-Color Progress",
        "Comet-style indeterminate circular progress bar",
        AnimScreen.MultiColorProgress,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Wave Loading Circle",
        "Liquid-fill circular loader — a sine wave rises from 0 to 100% inside a circle clip, with amplitude damped near empty and full.",
        AnimScreen.WaveLoadingCircle,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Gradient Progress Bar",
        "Arc-based progress with linear gradient",
        AnimScreen.GradientProgress,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Path Progress",
        "Animated path-drawing border button and progress bar",
        AnimScreen.PathProgress,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "File Delete animation",
        "File delete Animation",
        AnimScreen.FileDeleteAnimation,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Button Animation",
        "Spring-driven button press animation",
        AnimScreen.ButtonAnimation,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Strava Save Activity",
        "Pill button morphs to circular loader, then point-sample lerps into a checkmark.",
        AnimScreen.SaveActivity,
        FeatureGroup.PROGRESS_BUTTONS
    ),
    AnimationCategory(
        "Google Calling Animation",
        "Mimics the Google Dialer calling animation with bouncing FAB and sliding/fading arrows.",
        AnimScreen.GoogleCalling,
        FeatureGroup.PROGRESS_BUTTONS
    ),

    // ── Text & Typography ───────────────────────────────────────────────────
    AnimationCategory(
        "Marquee Text",
        "Horizontally scrolling overflowing text with gradient edges",
        AnimScreen.MarqueeDemo,
        FeatureGroup.TEXT_TYPOGRAPHY
    ),
    AnimationCategory(
        "Text Shimmer Effects",
        "Premium shimmer, wave, and spotlight text animations",
        AnimScreen.TextShimmer,
        FeatureGroup.TEXT_TYPOGRAPHY
    ),
    AnimationCategory(
        "Squiggly Spans Math",
        "Understanding animated squiggly underlines via Sine waves",
        AnimScreen.SquigglySpans,
        FeatureGroup.TEXT_TYPOGRAPHY
    ),
    AnimationCategory(
        "Squiggly Slider",
        "Material Expressive style slider with a wavy animated progress track.",
        AnimScreen.SquigglySlider,
        FeatureGroup.TEXT_TYPOGRAPHY
    ),

    // ── App Clones & Real-world ─────────────────────────────────────────────
    AnimationCategory(
        "Google Calendar Clone",
        "Full-featured calendar with schedule, day, and week views. Collapsible month toolbar, overlapping event layout, CalendarProvider integration.",
        AnimScreen.GoogleCalendar,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Breathing (Headspace)",
        "Tap to play/pause — layered wavy gradient, an organic morphing blob, and a parametric play↔pause morph.",
        AnimScreen.Breathing,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Apple Activity Rings",
        "Three concentric rings fill (some past 100%) with rounded caps and an end-cap shadow. Tap to replay.",
        AnimScreen.ActivityRings,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Apple Wallet — Collapsing Cards",
        "Scroll-driven sticky card stack — cards pile at the top with scale & fade. Port of wcandillon's Wallet.",
        AnimScreen.WalletStack,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "FlightSeat (Compose port)",
        "Port of ldoublem/FlightSeat — top-down plane with tap-to-select seats, live count, side minimap.",
        AnimScreen.FlightSeat,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "3D Card Flip",
        "Realistic credit card flip with gloss and shadows",
        AnimScreen.CardFlip,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Biometric Animation",
        "Fingerprint scanner with idle / scanning / success / error states",
        AnimScreen.BiometricDemo,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Date Picker",
        "Horizontal Date Picker",
        AnimScreen.DatePickerScreen,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Calendar Picker",
        "Month-view calendar with day selection and a custom CalendarViewModel",
        AnimScreen.CalendarPicker,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Passkeys Demo",
        "Modern passwordless authentication using FIDO2 and Credential Manager.",
        AnimScreen.Passkeys,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Protobuf over HTTP",
        "Fetch a contact list from a local desktop server encoded as Protocol Buffers; decode it with generated classes. Run ./gradlew :server:run first.",
        AnimScreen.ProtobufDemo,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "RemoteCompose (server-driven UI)",
        "The desktop server BUILDS a RemoteCompose document (androidx.compose.remote) in JVM Kotlin and ships the bytes; the app downloads and plays them — the layout lives on the server. Run ./gradlew :server:run first.",
        AnimScreen.RemoteComposeDemo,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Memory Profiler (Android 16 · ProfilingManager)",
        "Diagnostics dashboard for the Baklava (API 36) ProfilingManager: live heap metrics, ANR/fully-drawn triggers, on-demand heap dumps and a memory-spike simulator. Requires Android 16+ for triggers.",
        AnimScreen.MemoryProfiler,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Multi-Language Localization",
        "Dynamic in-app locale switching (English, Hindi, Telugu, Kannada, Marathi, Gujarati) with Material 3 components.",
        AnimScreen.LanguageSample,
        FeatureGroup.APP_CLONES
    ),
    AnimationCategory(
        "Gemini Flash STT",
        "Real-time audio transcription using Firebase AI Logic and Gemini 2.0 Flash.",
        AnimScreen.GeminiStt,
        FeatureGroup.APP_CLONES
    )
)
