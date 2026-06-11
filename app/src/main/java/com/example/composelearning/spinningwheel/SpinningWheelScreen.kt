package com.example.composelearning.spinningwheel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.launch

/** Decelerating ease-out: fast launch, gentle friction-like stop. */
private val SpinEasing = CubicBezierEasing(0.1f, 0.85f, 0.2f, 1f)
private const val SPIN_DURATION_MS = 4200

@Composable
fun SpinningWheelRoute(
    modifier: Modifier = Modifier,
    sections: List<WheelSection> = defaultWheelSections
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<WheelSection?>(null) }

    SpinningWheelContent(
        rotation = rotation.value,
        sections = sections,
        isSpinning = isSpinning,
        result = result,
        onSpin = {
            if (isSpinning) return@SpinningWheelContent
            isSpinning = true
            result = null
            scope.launch {
                val target = nextSpinTarget(rotation.value)
                rotation.animateTo(
                    targetValue = target,
                    animationSpec = tween(SPIN_DURATION_MS, easing = SpinEasing)
                )
                result = sections[winningIndex(target, sections.size)]
                isSpinning = false
            }
        },
        modifier = modifier
    )
}

/**
 * Next absolute rotation: keep continuity from [current], add several full
 * turns plus a random landing offset so the outcome is a fair random draw.
 */
private fun nextSpinTarget(current: Float): Float {
    val base = current - (current % 360f)
    val fullSpins = 360f * Random.nextInt(4, 7)
    val extra = Random.nextFloat() * 360f
    return base + fullSpins + extra
}

/** Section under the top pointer (270°) once the wheel stops at [target]. */
private fun winningIndex(target: Float, count: Int): Int {
    val sweep = 360f / count
    val finalAngle = ((target % 360f) + 360f) % 360f
    val pointerRel = ((270f - finalAngle) % 360f + 360f) % 360f
    return (pointerRel / sweep).toInt() % count
}

@Composable
private fun SpinningWheelContent(
    rotation: Float,
    sections: List<WheelSection>,
    isSpinning: Boolean,
    result: WheelSection?,
    onSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Spin the Wheel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Box(contentAlignment = Alignment.Center) {
            SpinningWheel(
                rotation = rotation,
                sections = sections,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ResultCard(isSpinning = isSpinning, result = result)

        Button(
            onClick = onSpin,
            enabled = !isSpinning,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(
                text = if (isSpinning) "Spinning…" else "SPIN",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ResultCard(isSpinning: Boolean, result: WheelSection?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = result?.color?.copy(alpha = 0.15f)
                ?: MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .animateContentSize()
    ) {
        AnimatedContent(
            targetState = when {
                isSpinning -> "spinning"
                result != null -> "result"
                else -> "idle"
            },
            label = "result"
        ) { state ->
            val text = when (state) {
                "spinning" -> "Good luck!"
                "result" -> "You won: ${result?.label?.replace("\n", " ")}"
                else -> "Tap SPIN to play"
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpinningWheelScreenPreview() {
    SpinningWheelRoute()
}
