package com.example.composelearning.animcompose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun PhysicsDatePicker(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    // State management
    val today = remember { LocalDate.now() }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Generate 7 days centered on today
    val dates = remember {
        (-3..3).map { offset -> today.plusDays(offset.toLong()) }
    }

    // Spring animation specs - PHYSICS BASED!
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy, // 0.6f - bouncy but settles
        stiffness = Spring.StiffnessLow // 150f - slow, smooth movement
    )

    // Animate the offset with spring physics
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = springSpec,
        label = "offset"
    )

    // Gradient colors for the center highlight
    val centerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFF1a1a2e)),
        contentAlignment = Alignment.Center
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(80.dp)
                .background(
                    color = Color(0xFF16213e),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // The center highlight indicator (animated with gradient)
        // We use AnimatedVisibility to provide the AnimatedVisibilityScope required by animateEnterExit
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = EnterTransition.None,
            exit = ExitTransition.None
        ) {
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset.dp)
                    .size(70.dp)
                    .background(
                        brush = centerGradient,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .animateEnterExit(
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    )
            )
        }

        // Date items row
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragOffset += delta * 0.3f // Dampen the drag
                    },
                    onDragStopped = { velocity ->
                        // Physics-based snap to nearest date
                        val itemWidth = 90f // approximate width per item
                        val targetIndex = (-animatedOffset / itemWidth).toInt()
                            .coerceIn(-3, 3)

                        selectedIndex = targetIndex + 3 // Adjust for list index
                        dragOffset = -(targetIndex * itemWidth)

                        onDateSelected(dates[selectedIndex])
                    }
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dates.forEachIndexed { index, date ->
                val distanceFromCenter = abs(index - 3 + (animatedOffset / 90f))
                val isCenter = index == selectedIndex && abs(animatedOffset % 90) < 10

                DateItem(
                    date = date,
                    distanceFromCenter = distanceFromCenter,
                    isCenter = isCenter,
                    animatedOffset = animatedOffset
                )
            }
        }
    }
}

@Composable
private fun DateItem(
    date: LocalDate,
    distanceFromCenter: Float,
    isCenter: Boolean,
    animatedOffset: Float
) {
    // Physics-based scale animation
    val scale by animateFloatAsState(
        targetValue = if (isCenter) 1.2f else 1f - (distanceFromCenter * 0.15f).coerceIn(0f, 0.4f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Alpha fades as we move away from center
    val alpha by animateFloatAsState(
        targetValue = if (isCenter) 1f else 0.6f - (distanceFromCenter * 0.1f).coerceIn(0f, 0.4f),
        animationSpec = tween(300),
        label = "alpha"
    )

    // Gradient text effect for center item
    val textColor = if (isCenter) {
        Color.White
    } else {
        Color(0xFFa0a0a0)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        // Day of week
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEE")),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Date number
        Text(
            text = date.dayOfMonth.toString(),
            style = TextStyle(
                fontSize = if (isCenter) 28.sp else 20.sp,
                fontWeight = if (isCenter) FontWeight.ExtraBold else FontWeight.Medium,
                color = textColor
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Month
        Text(
            text = date.format(DateTimeFormatter.ofPattern("MMM")),
            style = TextStyle(
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        )
    }
}
