package com.example.composelearning.animcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AnimationCategory(
    val title: String,
    val description: String,
    val route: AnimScreen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(navigator: Navigator) {
    val categories = listOf(
        AnimationCategory(
            "Tutorial Overlay",
            "Coach-mark walkthrough on a LazyColumn — auto-scrolls to off-screen targets, animated cutout, callout card",
            AnimScreen.TutorialOverlay
        ),
        AnimationCategory(
            "AGSL Shader Demos",
            "Blur, frosted glass, mesh gradient, shimmer, liquid button, film grain",
            AnimScreen.ShaderDemos
        ),
        AnimationCategory(
            "Speedometer",
            "Arc speedometer with animated needle and color thresholds",
            AnimScreen.Speedometer
        ),
        AnimationCategory(
            "Fitness — Continuous Line Chart",
            "Paged line chart, scroll left to load older days. RecyclerView-decorator style.",
            AnimScreen.FitnessLineChart
        ),
        AnimationCategory(
            "Bar Chart",
            "Grouped + stacked, gradient bars, tap to select",
            AnimScreen.BarChartDemo
        ),
        AnimationCategory(
            "Donut Chart",
            "Animated sweep, center slot, tap-to-select segment, legend",
            AnimScreen.DonutChartDemo
        ),
        AnimationCategory(
            "Candle Chart",
            "OHLC bars with wicks, volume strip, animated entry",
            AnimScreen.CandleChartDemo
        ),
        AnimationCategory(
            "Thermometer & Temperature",
            "Gauge, Material thermometer, and animated medical thermometer — tabbed",
            AnimScreen.TemperatureGaugeDemo
        ),
        AnimationCategory(
            "Bezier Curves",
            "Deep dive + interactive Explorer (quadratic, cubic, Figma → Compose) — tabbed",
            AnimScreen.BezierCurves
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
            "Particle Explosion",
            "Continuous stream of particles with scaling and alpha",
            AnimScreen.ParticleExplosion
        ),
        AnimationCategory(
            "Realistic Physics Explosion",
            "Gravity and drag based particle system",
            AnimScreen.RealisticExplosion
        ),
        AnimationCategory(
            "Staggered Grid Animation",
            "Grid items appearing with delayed entrance",
            AnimScreen.StaggeredGrid
        ),
        AnimationCategory(
            "Sine Wave Path",
            "Animating segments of a mathematical sine wave",
            AnimScreen.SineWavePath
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
            "Math Basics",
            "Coordinate systems, vectors, trigonometry",
            AnimScreen.MathBasics
        ),
        AnimationCategory(
            "Drawing Fundamentals",
            "Canvas, DrawScope, coordinate systems",
            AnimScreen.DrawingFundamentals
        ),
        AnimationCategory(
            "Lines, Shapes & Arcs",
            "Primitives and basic shapes",
            AnimScreen.LinesShapesArcs
        ),
        AnimationCategory(
            "Paths & Complex Shapes",
            "Bezier curves and custom paths",
            AnimScreen.PathsComplexShapes
        ),
        AnimationCategory(
            "Images & Bitmaps",
            "Working with images on canvas",
            AnimScreen.ImagesBitmaps
        ),
        AnimationCategory(
            "Canvas State",
            "Save/restore, transformations",
            AnimScreen.CanvasState
        ),
        AnimationCategory(
            "Touch & Gestures",
            "Drag, pinch, multi-touch",
            AnimScreen.TouchGestures
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
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jetpack Compose Animations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            items(categories) { category ->
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
