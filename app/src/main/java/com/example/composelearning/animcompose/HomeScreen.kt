package com.example.composelearning.animcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class AnimationCategory(
    val title: String,
    val description: String,
    val route: com.example.composelearning.AnimScreen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(navigator: Navigator) {
    val categories = remember { listOf(
        AnimationCategory(
            "Spinning Wheel",
            "Spinning wheel",
            com.example.composelearning.AnimScreen.SpinningWheel,
        ),
        AnimationCategory(
            "Particle Hub",
            "Consolidated particle systems: 3D Explosion, Continuous Stream, Realistic Physics, and Fireworks.",
            com.example.composelearning.AnimScreen.ParticleHub,
        ),
        AnimationCategory(
            "Charts & Waves Hub",
            "Consolidated charting showcase: Line, Bar, Donut, Pie, Candle, Speedometer, Temperature, and Sine Waves.",
            com.example.composelearning.AnimScreen.ChartsHub,
        ),
        AnimationCategory(
            "Canvas Basics Hub",
            "Consolidated fundamental drawing concepts: Math, Drawing primitives, Paths, Bitmaps, Canvas State, and Gestures.",
            com.example.composelearning.AnimScreen.CanvasBasicsHub,
        ),
        AnimationCategory(
            "Nav3 — Tabs + Shared Elements",
            "Single NavDisplay with per-tab back stacks (Photos / Articles / Profile). Bottom bar hides on detail screens.",
            com.example.composelearning.AnimScreen.TabsSample,
        ),
        AnimationCategory(
            "Top-Right Fan Carousel",
            "Stacked-card carousel — front card centered, others fanned toward the top-right. Drag-to-dismiss.",
            com.example.composelearning.AnimScreen.FanCarousel,
        ),
        AnimationCategory(
            "Arc Carousel (Swiggy Instamart)",
            "LazyRow + snap fling laid out along a dome curve at the bottom — center item raised and highlighted in a circle.",
            com.example.composelearning.AnimScreen.ArcCarousel,
        ),
        AnimationCategory(
            "FlightSeat (Compose port)",
            "Port of ldoublem/FlightSeat — top-down plane with tap-to-select seats, live count, side minimap.",
            com.example.composelearning.AnimScreen.FlightSeat,
        ),
        AnimationCategory(
            "SmoothProgressBar (Compose port)",
            "Port of castorflex/SmoothProgressBar — indeterminate horizontal sections sliding with cycling colors.",
            com.example.composelearning.AnimScreen.SmoothProgress,
        ),
        AnimationCategory(
            "Per-item ViewModels (Compose)",
            "Scope a ViewModel to one list item or pager page. LazyColumn + HorizontalPager demo.",
            com.example.composelearning.AnimScreen.PerItemViewModel,
        ),
        AnimationCategory(
            "Strava Save Activity",
            "Pill button morphs to circular loader, then point-sample lerps into a checkmark.",
            com.example.composelearning.AnimScreen.SaveActivity,
        ),
        AnimationCategory(
            "Zoomable Image",
            "Pinch-to-zoom and pan with rememberTransformableState — double-tap to reset.",
            com.example.composelearning.AnimScreen.ZoomableImage,
        ),
        AnimationCategory(
            "Image Processing (AGSL)",
            "Instagram-style filters rendered as an AGSL RuntimeShader RenderEffect.",
            com.example.composelearning.AnimScreen.ImageProcessing,
        ),
        AnimationCategory(
            "Sort Animations",
            "Tabbed hub: bubble, quick, insertion, selection, shell, merge, heap, and Timsort.",
            com.example.composelearning.AnimScreen.SortAnimation,
        ),
        AnimationCategory(
            "Netflix — Shape redraw",
            "Path + clipPath + gradient sweep. Clean geometry, ~120 LOC. Tap to replay.",
            com.example.composelearning.AnimScreen.NetflixLogo,
        ),
        AnimationCategory(
            "Netflix — Paint redraw (Anmol port)",
            "Port of @anmolverma's compose-animation-examples: 31 gradient strips + parallel keyframe tracks.",
            com.example.composelearning.AnimScreen.AnmolNetflix,
        ),
        AnimationCategory(
            "Calendar Picker",
            "Month-view calendar with day selection and a custom CalendarViewModel",
            com.example.composelearning.AnimScreen.CalendarPicker,
        ),
        AnimationCategory(
            "Biometric Animation",
            "Fingerprint scanner with idle / scanning / success / error states",
            com.example.composelearning.AnimScreen.BiometricDemo,
        ),
        AnimationCategory(
            "Button Animation",
            "Spring-driven button press animation",
            com.example.composelearning.AnimScreen.ButtonAnimation,
        ),
        AnimationCategory(
            "Blur Effects",
            "Modifier.blur and Haze frosted-glass demos",
            com.example.composelearning.AnimScreen.BlurEffects,
        ),
        AnimationCategory(
            "Lists Showcase",
            "12 tabbed list demos: alerts, products, sticky, reorder, swipe, staggered, news, circular.",
            com.example.composelearning.AnimScreen.ListsShowcase,
        ),
        AnimationCategory(
            "Animated Entry List",
            "LazyColumn rows fade + slide onto position as they appear; first batch cascades in (staggered).",
            com.example.composelearning.AnimScreen.AnimatedListEntry,
        ),
        AnimationCategory(
            "Pager & Carousel Showcase",
            "Tabbed showcase: Instagram coverflow, Instagram v2, HorizontalPager demo.",
            com.example.composelearning.AnimScreen.PagerShowcase,
        ),
        AnimationCategory(
            "Percentage Layout",
            "BoxWithConstraints + percentage-based offset positioning",
            com.example.composelearning.AnimScreen.PercentageLayout,
        ),
        AnimationCategory(
            "Path Progress",
            "Animated path-drawing border button and progress bar",
            com.example.composelearning.AnimScreen.PathProgress,
        ),
        AnimationCategory(
            "Marquee Text",
            "Horizontally scrolling overflowing text with gradient edges",
            com.example.composelearning.AnimScreen.MarqueeDemo,
        ),
        AnimationCategory(
            "Overlapping Images",
            "Custom Layout that stacks avatars with a slider-controlled overlap factor",
            com.example.composelearning.AnimScreen.OverlappingImages,
        ),
        AnimationCategory(
            "Time Range Knob",
            "Circular 24h dial with two draggable knobs — drag to set bedtime and wake-up.",
            com.example.composelearning.AnimScreen.TimeRangeKnob
        ),
        AnimationCategory(
            "Shadow Playground",
            "Every Compose shadow: elevation, colored ambient/spot, dropShadow, innerShadow, brush.",
            com.example.composelearning.AnimScreen.ShadowsPlayground
        ),
        AnimationCategory(
            "Tutorial Overlay",
            "Coach-mark walkthrough on a LazyColumn — auto-scrolls to off-screen targets.",
            com.example.composelearning.AnimScreen.TutorialOverlay
        ),
        AnimationCategory(
            "AGSL Shader Demos",
            "Blur, frosted glass, mesh gradient, shimmer, liquid button, film grain",
            com.example.composelearning.AnimScreen.ShaderDemos
        ),
        AnimationCategory(
            "Riveo — Page Curl (AGSL)",
            "Port of wcandillon's Skia page curl — drag a card to peel the page over a cylinder; springs back on release.",
            com.example.composelearning.AnimScreen.RiveoPageCurl
        ),
        AnimationCategory(
            "Apple Wallet — Collapsing Cards",
            "Scroll-driven sticky card stack — cards pile at the top with scale & fade. Port of wcandillon's Wallet.",
            com.example.composelearning.AnimScreen.WalletStack
        ),
        AnimationCategory(
            "Breathing (Headspace)",
            "Tap to play/pause — layered wavy gradient, an organic morphing blob, and a parametric play↔pause morph.",
            com.example.composelearning.AnimScreen.Breathing
        ),
        AnimationCategory(
            "SVG Path Morphing",
            "Drag a slider to morph phone silhouettes across eras via per-coordinate SVG path interpolation.",
            com.example.composelearning.AnimScreen.PathMorph
        ),
        AnimationCategory(
            "Apple Activity Rings",
            "Three concentric rings fill (some past 100%) with rounded caps and an end-cap shadow. Tap to replay.",
            com.example.composelearning.AnimScreen.ActivityRings
        ),
        AnimationCategory(
            "Fold Card (pinch)",
            "Pinch vertically to bend a card in half in 3D — two faces fold about the crease with perspective + shading.",
            com.example.composelearning.AnimScreen.FoldCard
        ),
        AnimationCategory(
            "iPod Click Wheel",
            "Drag around the wheel to scroll/highlight songs; center button selects. Rotary angular-delta scrolling.",
            com.example.composelearning.AnimScreen.IpodWheel
        ),
        AnimationCategory(
            "Clear To-Do (pinch-create)",
            "Pinch two rows apart to unfold a 'Create a new Task' row in the gap; release past threshold to insert.",
            com.example.composelearning.AnimScreen.ClearTodo
        ),
        AnimationCategory(
            "Protobuf over HTTP",
            "Fetch a contact list from a local desktop server encoded as Protocol Buffers; decode it with generated classes. Run ./gradlew :server:run first.",
            com.example.composelearning.AnimScreen.ProtobufDemo
        ),
        AnimationCategory(
            "Product Shared Elements",
            "Cinema-style transitions between product list and details",
            com.example.composelearning.AnimScreen.SharedElementProduct
        ),
        AnimationCategory(
            "Chat App Navigation",
            "Shared element transitions in a messaging UI",
            com.example.composelearning.AnimScreen.ChatApp
        ),
        AnimationCategory(
            "Fluid Tab Bar",
            "Morphing indicators and spring-based interactions",
            com.example.composelearning.AnimScreen.FluidTabs
        ),
        AnimationCategory(
            "3D Card Flip",
            "Realistic credit card flip with gloss and shadows",
            com.example.composelearning.AnimScreen.CardFlip
        ),
        AnimationCategory(
            "Sensor Reactive Card",
            "Credit card that tilts based on device sensors",
            com.example.composelearning.AnimScreen.SensorCard
        ),
        AnimationCategory(
            "Spotlight Walkthrough",
            "Interactive feature highlighting overlay system",
            com.example.composelearning.AnimScreen.Spotlight
        ),
        AnimationCategory(
            "Text Shimmer Effects",
            "Premium shimmer, wave, and spotlight text animations",
            com.example.composelearning.AnimScreen.TextShimmer
        ),
        AnimationCategory(
            "Circular Menu",
            "Animated menu buttons emerging from center",
            com.example.composelearning.AnimScreen.CircleMenu
        ),
        AnimationCategory(
            "YouTube Style Screen",
            "Complex layout with custom concave shapes and nested scrolling",
            com.example.composelearning.AnimScreen.YouTubeStyle
        ),
        AnimationCategory(
            "Staggered Grid Animation",
            "Grid items appearing with delayed entrance",
            com.example.composelearning.AnimScreen.StaggeredGrid
        ),
        AnimationCategory(
            "Circular Reveal",
            "Expanding circular path reveal (WhatsApp style)",
            com.example.composelearning.AnimScreen.CircularReveal
        ),
        AnimationCategory(
            "Draggable Side Sheet",
            "Panel that pulls out from the right side of the screen",
            com.example.composelearning.AnimScreen.DraggableSheet
        ),
        AnimationCategory(
            "Pulsating Circles",
            "Multiple overlapping pulse animations",
            com.example.composelearning.AnimScreen.PulsatingCircles
        ),
        AnimationCategory(
            "Multi-Color Progress",
            "Comet-style indeterminate circular progress bar",
            com.example.composelearning.AnimScreen.MultiColorProgress
        ),
        AnimationCategory(
            "Gradient Progress Bar",
            "Arc-based progress with linear gradient",
            com.example.composelearning.AnimScreen.GradientProgress
        ),
        AnimationCategory(
            "Bouncing Ball",
            "Vertical bounce animation with screen boundaries",
            com.example.composelearning.AnimScreen.BouncingBall
        ),
        AnimationCategory(
            "Animated Balance Counter",
            "Count-up balance animation from 0 to target",
            com.example.composelearning.AnimScreen.AnimatedBalance
        ),
        AnimationCategory(
            "Stacked Tinder Cards",
            "Swipeable cards with interaction physics",
            com.example.composelearning.AnimScreen.StackedCards
        ),
        AnimationCategory(
            "April 2026 Updates",
            "New features: Morphing Shapes, PullToRefreshBox, Shared Elements",
            com.example.composelearning.AnimScreen.April2026Features
        ),
        AnimationCategory(
            "Animation Basics",
            "Value-based and infinite animations",
            com.example.composelearning.AnimScreen.AnimationBasics
        ),
        AnimationCategory(
            "Value-Based Animations",
            "Custom types and keyframes",
            com.example.composelearning.AnimScreen.ValueBasedAnimations
        ),
        AnimationCategory(
            "Transition Animations",
            "State-driven animations",
            com.example.composelearning.AnimScreen.TransitionAnimations
        ),
        AnimationCategory(
            "Physics Animations",
            "Spring, decay, fling",
            com.example.composelearning.AnimScreen.PhysicsAnimations
        ),
        AnimationCategory(
            "Physics Game",
            "Bubble Pop Game",
            com.example.composelearning.AnimScreen.GameEnvironment
        ),
        AnimationCategory(
            "Bottle Wave Animation",
            "Animating bottle wave filling",
            com.example.composelearning.AnimScreen.BottleWaveAnimation
        ),
        AnimationCategory(
            "Date Picker",
            "Horizontal Date Picker",
            com.example.composelearning.AnimScreen.DatePickerScreen
        ),
        AnimationCategory(
            "File Delete animation",
            "File delete Animation",
            com.example.composelearning.AnimScreen.FileDeleteAnimation
        ),
        AnimationCategory(
            "Squiggly Spans Math",
            "Understanding animated squiggly underlines via Sine waves",
            com.example.composelearning.AnimScreen.SquigglySpans
        ),
        AnimationCategory(
            "Squiggly Slider",
            "Material Expressive style slider with a wavy animated progress track.",
            com.example.composelearning.AnimScreen.SquigglySlider
        ),
        AnimationCategory(
            "Passkeys Demo",
            "Modern passwordless authentication using FIDO2 and Credential Manager.",
            com.example.composelearning.AnimScreen.Passkeys
        ),
        AnimationCategory(
            "Arc List Navigation",
            "Interactive circular layout with drag-to-spin physics",
            com.example.composelearning.AnimScreen.ArcList
        ),
        AnimationCategory(
            "Google Calendar Clone",
            "Full-featured calendar with schedule, day, and week views. Collapsible month toolbar, overlapping event layout, CalendarProvider integration.",
            com.example.composelearning.AnimScreen.GoogleCalendar
        ),
        AnimationCategory(
            "Google Calling Animation",
            "Mimics the Google Dialer calling animation with bouncing FAB and sliding/fading arrows.",
            com.example.composelearning.AnimScreen.GoogleCalling
        ),
        AnimationCategory(
            "Mesh Gradient",
            "Demo for mesh gradient using compose",
            com.example.composelearning.AnimScreen.MeshGradient
        ),
        AnimationCategory(
            "Analog watch dial",
            "Analog watch dial",
            com.example.composelearning.AnimScreen.AnimatingWatchDial
        )
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jetpack Compose Animations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White,
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = categories,
                key = { it.title },
            ) { category ->
                CategoryCard(
                    title = category.title,
                    description = category.description,
                    onClick = { navigator.navigate(category.route) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
