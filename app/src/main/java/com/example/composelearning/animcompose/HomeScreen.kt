package com.example.composelearning.animcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


data class AnimationCategory(
    val title: String,
    val description: String,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val categories = listOf(
        AnimationCategory(
            "Math Basics",
            "Coordinate systems, vectors, trigonometry",
            AnimScreen.MathBasics.route
        ),
        AnimationCategory(
            "Drawing Fundamentals",
            "Canvas, DrawScope, coordinate systems",
            AnimScreen.DrawingFundamentals.route
        ),
        AnimationCategory(
            "Lines, Shapes & Arcs",
            "Primitives and basic shapes",
            AnimScreen.LinesShapesArcs.route
        ),
        AnimationCategory(
            "Paths & Complex Shapes",
            "Bezier curves and custom paths",
            AnimScreen.PathsComplexShapes.route
        ),
        AnimationCategory(
            "Images & Bitmaps",
            "Working with images on canvas",
            AnimScreen.ImagesBitmaps.route
        ),
        AnimationCategory(
            "Canvas State",
            "Save/restore, transformations",
            AnimScreen.CanvasState.route
        ),
        AnimationCategory(
            "Touch & Gestures",
            "Drag, pinch, multi-touch",
            AnimScreen.TouchGestures.route
        ),
        AnimationCategory(
            "Animation Basics",
            "Value-based and infinite animations",
            AnimScreen.AnimationBasics.route
        ),
        AnimationCategory(
            "Value-Based Animations",
            "Custom types and keyframes",
            AnimScreen.ValueBasedAnimations.route
        ),
        AnimationCategory(
            "Transition Animations",
            "State-driven animations",
            AnimScreen.TransitionAnimations.route
        ),
        AnimationCategory(
            "Physics Animations",
            "Spring, decay, fling",
            AnimScreen.PhysicsAnimations.route
        ),
        AnimationCategory("Physics Game", "Bubble Pop Game", AnimScreen.GameEnvironment.route),
        AnimationCategory(
            "Bottle Wave Animation",
            "Animating bottle wave filling",
            AnimScreen.BottleWaveAnimation.route
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
                    onClick = { navController.navigate(category.route) }
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