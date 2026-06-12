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
    )
)
