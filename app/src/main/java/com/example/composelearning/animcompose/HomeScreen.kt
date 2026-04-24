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
            "Stacked Tinder Cards",
            "Swipeable cards with Coil images",
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
        ),
        AnimationCategory(
            "Thermometer Animation",
            "Medical thermometer visualization",
            AnimScreen.ThermometerAnimation
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
