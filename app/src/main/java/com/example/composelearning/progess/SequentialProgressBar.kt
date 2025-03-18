/*
package com.example.composelearning.progess

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SequentialMultiColorProgressBar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 8.dp,
    colors: List<Color> = listOf(Color.Red, Color.Green, Color.Blue),
    sweepAngles: List<Float> = listOf(120f, 120f, 120f), // Equal segments for simplicity
    animationDuration: Int = 2000 // Duration for one full cycle
) {
    require(colors.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 colors." }
    require(sweepAngles.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 sweep angles." }
    require(sweepAngles.sum() <= 360f) { "Sum of sweep angles must be less than or equal to 360." }

    val infiniteTransition = rememberInfiniteTransition(label = "SequentialMultiColorProgressBar")

    // Animate the overall progress from 0 to 3 (representing the 3 arcs)
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SequentialMultiColorProgressBarProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = size.toPx()
        val diameter = canvasSize - strokeWidth.toPx()
        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Calculate the sweep angle for each arc based on the overall progress
        val arc1Sweep = calculateSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
        val arc2Sweep = calculateSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
        val arc3Sweep = calculateSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])

        val arc1StartAngle = 270f
        val arc2StartAngle = arc1StartAngle + sweepAngles[0]
        val arc3StartAngle = arc2StartAngle + sweepAngles[1]

        val arc1TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
        val arc2TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
        val arc3TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])

        val arc1TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 0f, 1f, sweepAngles[0], arc1StartAngle)
        val arc2TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 1f, 2f, sweepAngles[1], arc2StartAngle)
        val arc3TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 2f, 3f, sweepAngles[2], arc3StartAngle)
        // Draw the arcs in sequence
        drawArc(
            color = colors[0],
            startAngle = arc1TrimmedStartAngle,
            sweepAngle = arc1TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )

        drawArc(
            color = colors[1],
            startAngle = arc2TrimmedStartAngle,
            sweepAngle = arc2TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )

        drawArc(
            color = colors[2],
            startAngle = arc3TrimmedStartAngle,
            sweepAngle = arc3TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )
    }
}

// Helper function to calculate the sweep angle for each arc
private fun calculateSweepAngle(
    progress: Float,
    start: Float,
    end: Float,
    maxSweep: Float
): Float {
    return when {
        progress < start -> 0f
        progress > end -> maxSweep
        else -> (progress - start) / (end - start) * maxSweep
    }
}

// Helper function to calculate the trimmed sweep angle for each arc
private fun calculateTrimmedSweepAngle(
    progress: Float,
    start: Float,
    end: Float,
    maxSweep: Float
): Float {
    val sweep = calculateSweepAngle(progress, start, end, maxSweep)
    return when {
        progress < start -> sweep
        progress > end -> 0f
        else -> sweep
    }
}

// Helper function to calculate the trimmed start angle for each arc
private fun calculateTrimmedStartAngle(
    progress: Float,
    start: Float,
    end: Float,
    maxSweep: Float,
    originalStartAngle: Float
): Float {
    return when {
        progress < start -> originalStartAngle
        progress > end -> originalStartAngle + maxSweep
        else -> originalStartAngle
    }
}



@Composable
fun SequentialMultiColorProgressBar2(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 8.dp,
    colors: List<Color> = listOf(Color.Red, Color.Green, Color.Blue),
    sweepAngles: List<Float> = listOf(120f, 120f, 120f), // Equal segments for simplicity
    animationDuration: Int = 2000 // Duration for one full cycle
) {
    require(colors.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 colors." }
    require(sweepAngles.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 sweep angles." }
    require(sweepAngles.sum() <= 360f) { "Sum of sweep angles must be less than or equal to 360." }

    val infiniteTransition = rememberInfiniteTransition(label = "SequentialMultiColorProgressBar")

    // Animate the overall progress from 0 to 3 (representing the 3 arcs)
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SequentialMultiColorProgressBarProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = size.toPx()
        val diameter = canvasSize - strokeWidth.toPx()
        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Calculate the sweep angle for each arc based on the overall progress
        val arc1Sweep = calculateSweepAngle2(animatedProgress, 0f, 1f, sweepAngles[0])
        val arc2Sweep = calculateSweepAngle2(animatedProgress, 1f, 2f, sweepAngles[1])
        val arc3Sweep = calculateSweepAngle2(animatedProgress, 2f, 3f, sweepAngles[2])

        val arc1StartAngle = 270f
        val arc2StartAngle = arc1StartAngle + sweepAngles[0]
        val arc3StartAngle = arc2StartAngle + sweepAngles[1]

        val arc1TrimmedSweep = calculateTrimmedSweepAngle2(animatedProgress, 0f, 1f, sweepAngles[0])
        val arc2TrimmedSweep = calculateTrimmedSweepAngle2(animatedProgress, 1f, 2f, sweepAngles[1])
        val arc3TrimmedSweep = calculateTrimmedSweepAngle2(animatedProgress, 2f, 3f, sweepAngles[2])

        // Draw the arcs in sequence
        drawArc(
            color = colors[0],
            startAngle = arc1StartAngle,
            sweepAngle = arc1TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )

        drawArc(
            color = colors[1],
            startAngle = arc2StartAngle,
            sweepAngle = arc2TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )

        drawArc(
            color = colors[2],
            startAngle = arc3StartAngle,
            sweepAngle = arc3TrimmedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )
    }
}

// Helper function to calculate the sweep angle for each arc
private fun calculateSweepAngle2(
    progress: Float,
    start: Float,
    end: Float,
    maxSweep: Float
): Float {
    return when {
        progress < start -> 0f
        progress > end -> maxSweep
        else -> (progress - start) / (end - start) * maxSweep
    }
}

// Helper function to calculate the trimmed sweep angle for each arc
private fun calculateTrimmedSweepAngle2(
    progress: Float,
    start: Float,
    end: Float,
    maxSweep: Float
): Float {
    val sweep = calculateSweepAngle(progress, start, end, maxSweep)
    return if (progress > start && progress < end) {
        val trimStart = start
        val trimEnd = end
        val trimProgress = progress
        val trimThreshold = trimEnd - 0.5f
        if (trimProgress > trimThreshold) {
            val trimAmount = (trimProgress - trimThreshold) / (trimEnd - trimThreshold)
            sweep - (maxSweep * trimAmount)
        } else {
            sweep
        }
    } else {
        sweep
    }
}*/
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun SequentialMultiColorProgressBar(
//    modifier: Modifier = Modifier,
//    size: Dp = 48.dp,
//    strokeWidth: Dp = 8.dp,
//    colors: List<Color> = listOf(Color.Red, Color.Green, Color.Blue),
//    sweepAngles: List<Float> = listOf(120f, 120f, 120f), // Equal segments for simplicity
//    animationDuration: Int = 2000 // Duration for one full cycle
//) {
//    require(colors.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 colors." }
//    require(sweepAngles.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 sweep angles." }
//    require(sweepAngles.sum() <= 360f) { "Sum of sweep angles must be less than or equal to 360." }
//
//    val infiniteTransition = rememberInfiniteTransition(label = "SequentialMultiColorProgressBar")
//
//    // Animate the overall progress from 0 to 3 (representing the 3 arcs)
//    val animatedProgress by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 3f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "SequentialMultiColorProgressBarProgress"
//    )
//
//    Canvas(modifier = modifier.size(size)) {
//        val canvasSize = size.toPx()
//        val diameter = canvasSize - strokeWidth.toPx()
//        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
//        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
//
//        // Calculate the sweep angle for each arc based on the overall progress
//        val arc1Sweep = calculateSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
//        val arc2Sweep = calculateSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
//        val arc3Sweep = calculateSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])
//
//        // Draw the arcs in sequence
//        drawArc(
//            color = colors[0],
//            startAngle = 270f,
//            sweepAngle = arc1Sweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[1],
//            startAngle = 270f + sweepAngles[0],
//            sweepAngle = arc2Sweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[2],
//            startAngle = 270f + sweepAngles[0] + sweepAngles[1],
//            sweepAngle = arc3Sweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//    }
//}
//
//// Helper function to calculate the sweep angle for each arc
//private fun calculateSweepAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float
//): Float {
//    return when {
//        progress < start -> 0f
//        progress > end -> maxSweep
//        else -> (progress - start) / (end - start) * maxSweep
//    }
//}

//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun SequentialMultiColorProgressBar(
//    modifier: Modifier = Modifier,
//    size: Dp = 48.dp,
//    strokeWidth: Dp = 8.dp,
//    colors: List<Color> = listOf(Color.Red, Color.Green, Color.Blue),
//    sweepAngles: List<Float> = listOf(120f, 120f, 120f), // Equal segments for simplicity
//    animationDuration: Int = 2000 // Duration for one full cycle
//) {
//    require(colors.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 colors." }
//    require(sweepAngles.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 sweep angles." }
//    require(sweepAngles.sum() <= 360f) { "Sum of sweep angles must be less than or equal to 360." }
//
//    val infiniteTransition = rememberInfiniteTransition(label = "SequentialMultiColorProgressBar")
//
//    // Animate the overall progress from 0 to 3 (representing the 3 arcs)
//    val animatedProgress by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 3f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "SequentialMultiColorProgressBarProgress"
//    )
//
//    Canvas(modifier = modifier.size(size)) {
//        val canvasSize = size.toPx()
//        val diameter = canvasSize - strokeWidth.toPx()
//        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
//        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
//
//        // Calculate the sweep angle for each arc based on the overall progress
//        val arc1Sweep = calculateSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
//        val arc2Sweep = calculateSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
//        val arc3Sweep = calculateSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])
//
//        val arc1StartAngle = 270f
//        val arc2StartAngle = arc1StartAngle + sweepAngles[0]
//        val arc3StartAngle = arc2StartAngle + sweepAngles[1]
//
//        val arc1TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
//        val arc2TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
//        val arc3TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])
//
//        val arc1TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 0f, 1f, sweepAngles[0], arc1StartAngle, 1f)
//        val arc2TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 1f, 2f, sweepAngles[1], arc2StartAngle, 2f)
//        val arc3TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 2f, 3f, sweepAngles[2], arc3StartAngle, 3f)
//        // Draw the arcs in sequence
//        drawArc(
//            color = colors[0],
//            startAngle = arc1TrimmedStartAngle,
//            sweepAngle = arc1TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[1],
//            startAngle = arc2TrimmedStartAngle,
//            sweepAngle = arc2TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[2],
//            startAngle = arc3TrimmedStartAngle,
//            sweepAngle = arc3TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//    }
//}
//
//// Helper function to calculate the sweep angle for each arc
//private fun calculateSweepAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float
//): Float {
//    return when {
//        progress < start -> 0f
//        progress > end -> maxSweep
//        else -> (progress - start) / (end - start) * maxSweep
//    }
//}
//
//// Helper function to calculate the trimmed sweep angle for each arc
//private fun calculateTrimmedSweepAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float
//): Float {
//    val sweep = calculateSweepAngle(progress, start, end, maxSweep)
//    return when {
//        progress < start -> sweep
//        progress > end -> 0f
//        else -> sweep
//    }
//}
//
//// Helper function to calculate the trimmed start angle for each arc
//private fun calculateTrimmedStartAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float,
//    originalStartAngle: Float,
//    nextArcStart:Float
//): Float {
//    return when {
//        progress < start -> originalStartAngle
//        progress > end -> originalStartAngle + maxSweep
//        else -> {
//            val trimStart = start
//            val trimEnd = end
//            val trimProgress = progress
//            val trimThreshold = nextArcStart - 0.5f
//            if (trimProgress > trimThreshold) {
//                val trimAmount = (trimProgress - trimThreshold) / (trimEnd - trimThreshold)
//                originalStartAngle + (maxSweep * trimAmount)
//            } else {
//                originalStartAngle
//            }
//        }
//    }
//}

//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun SequentialMultiColorProgressBar(
//    modifier: Modifier = Modifier,
//    size: Dp = 48.dp,
//    strokeWidth: Dp = 8.dp,
//    colors: List<Color> = listOf(Color.Red, Color.Green, Color.Blue),
//    sweepAngles: List<Float> = listOf(120f, 120f, 120f), // Equal segments for simplicity
//    animationDuration: Int = 2000 // Duration for one full cycle
//) {
//    require(colors.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 colors." }
//    require(sweepAngles.size == 3) { "SequentialMultiColorProgressBar requires exactly 3 sweep angles." }
//    require(sweepAngles.sum() <= 360f) { "Sum of sweep angles must be less than or equal to 360." }
//
//    val infiniteTransition = rememberInfiniteTransition(label = "SequentialMultiColorProgressBar")
//
//    // Animate the overall progress from 0 to 3 (representing the 3 arcs)
//    val animatedProgress by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 3f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "SequentialMultiColorProgressBarProgress"
//    )
//
//    Canvas(modifier = modifier.size(size)) {
//        val canvasSize = size.toPx()
//        val diameter = canvasSize - strokeWidth.toPx()
//        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
//        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
//
//        // Calculate the sweep angle for each arc based on the overall progress
//        val arc1Sweep = calculateSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
//        val arc2Sweep = calculateSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
//        val arc3Sweep = calculateSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])
//
//        val arc1StartAngle = 270f
//        val arc2StartAngle = arc1StartAngle + sweepAngles[0]
//        val arc3StartAngle = arc2StartAngle + sweepAngles[1]
//
//        val arc1TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 0f, 1f, sweepAngles[0])
//        val arc2TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 1f, 2f, sweepAngles[1])
//        val arc3TrimmedSweep = calculateTrimmedSweepAngle(animatedProgress, 2f, 3f, sweepAngles[2])
//
//        val arc1TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 0f, 1f, sweepAngles[0], arc1StartAngle, 1f)
//        val arc2TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 1f, 2f, sweepAngles[1], arc2StartAngle, 2f)
//        val arc3TrimmedStartAngle = calculateTrimmedStartAngle(animatedProgress, 2f, 3f, sweepAngles[2], arc3StartAngle, 3f)
//        // Draw the arcs in sequence
//        drawArc(
//            color = colors[0],
//            startAngle = arc1TrimmedStartAngle,
//            sweepAngle = arc1TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[1],
//            startAngle = arc2TrimmedStartAngle,
//            sweepAngle = arc2TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//
//        drawArc(
//            color = colors[2],
//            startAngle = arc3TrimmedStartAngle,
//            sweepAngle = arc3TrimmedSweep,
//            useCenter = false,
//            topLeft = topLeft,
//            size = Size(diameter, diameter),
//            style = stroke
//        )
//    }
//}
//
//// Helper function to calculate the sweep angle for each arc
//private fun calculateSweepAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float
//): Float {
//    return when {
//        progress < start -> 0f
//        progress > end -> maxSweep
//        else -> (progress - start) / (end - start) * maxSweep
//    }
//}
//
//// Helper function to calculate the trimmed sweep angle for each arc
//private fun calculateTrimmedSweepAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float
//): Float {
//    val sweep = calculateSweepAngle(progress, start, end, maxSweep)
//    return when {
//        progress < start -> sweep
//        progress > end -> 0f
//        else -> sweep
//    }
//}
//
//// Helper function to calculate the trimmed start angle for each arc
//private fun calculateTrimmedStartAngle(
//    progress: Float,
//    start: Float,
//    end: Float,
//    maxSweep: Float,
//    originalStartAngle: Float,
//    nextArcStart:Float
//): Float {
//    return when {
//        progress < start -> originalStartAngle
//        progress > end -> originalStartAngle + maxSweep
//        else -> {
//            val trimStart = start
//            val trimEnd = end
//            val trimProgress = progress
//            val trimThreshold = nextArcStart - 0.5f
//            if (trimProgress > trimThreshold) {
//                val trimAmount = (trimProgress - trimThreshold) / (trimEnd - trimThreshold)
//                originalStartAngle + (maxSweep * trimAmount)
//            } else {
//                originalStartAngle
//            }
//        }
//    }
//}

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CanvasCircularLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Color.Blue,
    animationDuration: Int = 1500 // Duration for one full cycle
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CanvasCircularLoader")

    // Animate the sweep angle from 0 to 360 and back to 0
    val animatedSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CanvasCircularLoaderSweep"
    )

    // Animate the start angle to create the rotation effect
    val animatedStartAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CanvasCircularLoaderStartAngle"
    )

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = size.toPx()
        val diameter = canvasSize - strokeWidth.toPx()
        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Draw the arc
        drawArc(
            color = color,
            startAngle = animatedStartAngle,
            sweepAngle = animatedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )
    }
}