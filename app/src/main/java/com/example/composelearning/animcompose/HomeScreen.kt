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
    val route: AnimScreen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(navigator: Navigator) {
    val categories = remember { listOf(
        AnimationCategory(
            "Particle Hub",
            "Consolidated particle systems: 3D Explosion, Continuous Stream, Realistic Physics, and Fireworks.",
            AnimScreen.ParticleHub,
        ),
        AnimationCategory(
            "Charts & Waves Hub",
            "Consolidated charting showcase: Line, Bar, Donut, Pie, Candle, Speedometer, Temperature, and Sine Waves.",
            AnimScreen.ChartsHub,
        ),
        AnimationCategory(
            "Canvas Basics Hub",
            "Consolidated fundamental drawing concepts: Math, Drawing primitives, Paths, Bitmaps, Canvas State, and Gestures.",
            AnimScreen.CanvasBasicsHub,
        ),
        AnimationCategory(
            "Nav3 — Tabs + Shared Elements",
            "Single NavDisplay with per-tab back stacks (Photos / Articles / Profile). Bottom bar hides on detail screens.",
            AnimScreen.TabsSample,
        ),
        AnimationCategory(
            "Top-Right Fan Carousel",
            "Stacked-card carousel — front card centered, others fanned toward the top-right. Drag-to-dismiss.",
            AnimScreen.FanCarousel,
        ),
        AnimationCategory(
            "Arc Carousel (Swiggy Instamart)",
            "LazyRow + snap fling laid out along a dome curve at the bottom — center item raised and highlighted in a circle.",
            AnimScreen.ArcCarousel,
        ),
        AnimationCategory(
            "FlightSeat (Compose port)",
            "Port of ldoublem/FlightSeat — top-down plane with tap-to-select seats, live count, side minimap.",
            AnimScreen.FlightSeat,
        ),
        AnimationCategory(
            "SmoothProgressBar (Compose port)",
            "Port of castorflex/SmoothProgressBar — indeterminate horizontal sections sliding with cycling colors.",
            AnimScreen.SmoothProgress,
        ),
        AnimationCategory(
            "Per-item ViewModels (Compose)",
            "Scope a ViewModel to one list item or pager page. LazyColumn + HorizontalPager demo.",
            AnimScreen.PerItemViewModel,
        ),
        AnimationCategory(
            "Strava Save Activity",
            "Pill button morphs to circular loader, then point-sample lerps into a checkmark.",
            AnimScreen.SaveActivity,
        ),
        AnimationCategory(
            "Zoomable Image",
            "Pinch-to-zoom and pan with rememberTransformableState — double-tap to reset.",
            AnimScreen.ZoomableImage,
        ),
        AnimationCategory(
            "Image Processing (AGSL)",
            "Instagram-style filters rendered as an AGSL RuntimeShader RenderEffect.",
            AnimScreen.ImageProcessing,
        ),
        AnimationCategory(
            "Sort Animations",
            "Tabbed hub: bubble, quick, insertion, selection, shell, merge, heap, and Timsort.",
            AnimScreen.SortAnimation,
        ),
        AnimationCategory(
            "Netflix — Shape redraw",
            "Path + clipPath + gradient sweep. Clean geometry, ~120 LOC. Tap to replay.",
            AnimScreen.NetflixLogo,
        ),
        AnimationCategory(
            "Netflix — Paint redraw (Anmol port)",
            "Port of @anmolverma's compose-animation-examples: 31 gradient strips + parallel keyframe tracks.",
            AnimScreen.AnmolNetflix,
        ),
        AnimationCategory(
            "Calendar Picker",
            "Month-view calendar with day selection and a custom CalendarViewModel",
            AnimScreen.CalendarPicker,
        ),
        AnimationCategory(
            "Biometric Animation",
            "Fingerprint scanner with idle / scanning / success / error states",
            AnimScreen.BiometricDemo,
        ),
        AnimationCategory(
            "Button Animation",
            "Spring-driven button press animation",
            AnimScreen.ButtonAnimation,
        ),
        AnimationCategory(
            "Blur Effects",
            "Modifier.blur and Haze frosted-glass demos",
            AnimScreen.BlurEffects,
        ),
        AnimationCategory(
            "Lists Showcase",
            "12 tabbed list demos: alerts, products, sticky, reorder, swipe, staggered, news, circular.",
            AnimScreen.ListsShowcase,
        ),
        AnimationCategory(
            "Pager & Carousel Showcase",
            "Tabbed showcase: Instagram coverflow, Instagram v2, HorizontalPager demo.",
            AnimScreen.PagerShowcase,
        ),
        AnimationCategory(
            "Percentage Layout",
            "BoxWithConstraints + percentage-based offset positioning",
            AnimScreen.PercentageLayout,
        ),
        AnimationCategory(
            "Path Progress",
            "Animated path-drawing border button and progress bar",
            AnimScreen.PathProgress,
        ),
        AnimationCategory(
            "Marquee Text",
            "Horizontally scrolling overflowing text with gradient edges",
            AnimScreen.MarqueeDemo,
        ),
        AnimationCategory(
            "Overlapping Images",
            "Custom Layout that stacks avatars with a slider-controlled overlap factor",
            AnimScreen.OverlappingImages,
        ),
        AnimationCategory(
            "Time Range Knob",
            "Circular 24h dial with two draggable knobs — drag to set bedtime and wake-up.",
            AnimScreen.TimeRangeKnob
        ),
        AnimationCategory(
            "Shadow Playground",
            "Every Compose shadow: elevation, colored ambient/spot, dropShadow, innerShadow, brush.",
            AnimScreen.ShadowsPlayground
        ),
        AnimationCategory(
            "Tutorial Overlay",
            "Coach-mark walkthrough on a LazyColumn — auto-scrolls to off-screen targets.",
            AnimScreen.TutorialOverlay
        ),
        AnimationCategory(
            "AGSL Shader Demos",
            "Blur, frosted glass, mesh gradient, shimmer, liquid button, film grain",
            AnimScreen.ShaderDemos
        ),
        AnimationCategory(
            "Riveo — Page Curl (AGSL)",
            "Port of wcandillon's Skia page curl — drag a card to peel the page over a cylinder; springs back on release.",
            AnimScreen.RiveoPageCurl
        ),
        AnimationCategory(
            "Product Shared Elements",
            "Cinema-style transitions between product list and details",
            AnimScreen.SharedElementProduct
        ),
        AnimationCategory(
            "Chat App Navigation",
            "Shared element transitions in a messaging UI",
            AnimScreen.ChatApp
        ),
        AnimationCategory(
            "Fluid Tab Bar",
            "Morphing indicators and spring-based interactions",
            AnimScreen.FluidTabs
        ),
        AnimationCategory(
            "3D Card Flip",
            "Realistic credit card flip with gloss and shadows",
            AnimScreen.CardFlip
        ),
        AnimationCategory(
            "Sensor Reactive Card",
            "Credit card that tilts based on device sensors",
            AnimScreen.SensorCard
        ),
        AnimationCategory(
            "Spotlight Walkthrough",
            "Interactive feature highlighting overlay system",
            AnimScreen.Spotlight
        ),
        AnimationCategory(
            "Text Shimmer Effects",
            "Premium shimmer, wave, and spotlight text animations",
            AnimScreen.TextShimmer
        ),
        AnimationCategory(
            "Circular Menu",
            "Animated menu buttons emerging from center",
            AnimScreen.CircleMenu
        ),
        AnimationCategory(
            "YouTube Style Screen",
            "Complex layout with custom concave shapes and nested scrolling",
            AnimScreen.YouTubeStyle
        ),
        AnimationCategory(
            "Staggered Grid Animation",
            "Grid items appearing with delayed entrance",
            AnimScreen.StaggeredGrid
        ),
        AnimationCategory(
            "Circular Reveal",
            "Expanding circular path reveal (WhatsApp style)",
            AnimScreen.CircularReveal
        ),
        AnimationCategory(
            "Draggable Side Sheet",
            "Panel that pulls out from the right side of the screen",
            AnimScreen.DraggableSheet
        ),
        AnimationCategory(
            "Pulsating Circles",
            "Multiple overlapping pulse animations",
            AnimScreen.PulsatingCircles
        ),
        AnimationCategory(
            "Multi-Color Progress",
            "Comet-style indeterminate circular progress bar",
            AnimScreen.MultiColorProgress
        ),
        AnimationCategory(
            "Gradient Progress Bar",
            "Arc-based progress with linear gradient",
            AnimScreen.GradientProgress
        ),
        AnimationCategory(
            "Bouncing Ball",
            "Vertical bounce animation with screen boundaries",
            AnimScreen.BouncingBall
        ),
        AnimationCategory(
            "Animated Balance Counter",
            "Count-up balance animation from 0 to target",
            AnimScreen.AnimatedBalance
        ),
        AnimationCategory(
            "Stacked Tinder Cards",
            "Swipeable cards with interaction physics",
            AnimScreen.StackedCards
        ),
        AnimationCategory(
            "April 2026 Updates",
            "New features: Morphing Shapes, PullToRefreshBox, Shared Elements",
            AnimScreen.April2026Features
        ),
        AnimationCategory(
            "Animation Basics",
            "Value-based and infinite animations",
            AnimScreen.AnimationBasics
        ),
        AnimationCategory(
            "Value-Based Animations",
            "Custom types and keyframes",
            AnimScreen.ValueBasedAnimations
        ),
        AnimationCategory(
            "Transition Animations",
            "State-driven animations",
            AnimScreen.TransitionAnimations
        ),
        AnimationCategory(
            "Physics Animations",
            "Spring, decay, fling",
            AnimScreen.PhysicsAnimations
        ),
        AnimationCategory(
            "Physics Game",
            "Bubble Pop Game",
            AnimScreen.GameEnvironment
        ),
        AnimationCategory(
            "Bottle Wave Animation",
            "Animating bottle wave filling",
            AnimScreen.BottleWaveAnimation
        ),
        AnimationCategory(
            "Date Picker",
            "Horizontal Date Picker",
            AnimScreen.DatePickerScreen
        ),
        AnimationCategory(
            "File Delete animation",
            "File delete Animation",
            AnimScreen.FileDeleteAnimation
        ),
        AnimationCategory(
            "Squiggly Spans Math",
            "Understanding animated squiggly underlines via Sine waves",
            AnimScreen.SquigglySpans
        ),
        AnimationCategory(
            "Squiggly Slider",
            "Material Expressive style slider with a wavy animated progress track.",
            AnimScreen.SquigglySlider
        ),
        AnimationCategory(
            "Passkeys Demo",
            "Modern passwordless authentication using FIDO2 and Credential Manager.",
            AnimScreen.Passkeys
        ),
        AnimationCategory(
            "Arc List Navigation",
            "Interactive circular layout with drag-to-spin physics",
            AnimScreen.ArcList
        ),
        AnimationCategory(
            "Google Calendar Clone",
            "Full-featured calendar with schedule, day, and week views. Collapsible month toolbar, overlapping event layout, CalendarProvider integration.",
            AnimScreen.GoogleCalendar
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
