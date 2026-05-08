package com.example.composelearning.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val BalanceTextStyle = TextStyle(
    fontSize = 48.sp,
    fontWeight = FontWeight.Bold,
    color = Color.White,
    fontFeatureSettings = "tnum",
)

@Composable
fun AnimatedBalance(
    balance: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 1500,
) {
    val animatedBalance = remember(balance) { Animatable(0f) }

    LaunchedEffect(balance) {
        animatedBalance.animateTo(
            targetValue = balance.toFloat(),
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = LinearOutSlowInEasing,
            ),
        )
    }

    val targetText = remember(balance) {
        NumberFormat.getNumberInstance(Locale.US).format(balance)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "₹ ", style = BalanceTextStyle)

        AnimatedNumberText(
            value = { animatedBalance.value },
            placeholder = targetText,
        )
    }
}

@Composable
private fun AnimatedNumberText(
    value: () -> Float,
    placeholder: String,
) {
    val displayValue = value().roundToInt()
    val text = remember(displayValue) {
        NumberFormat.getNumberInstance(Locale.US).format(displayValue)
    }

    Box(contentAlignment = Alignment.CenterEnd) {
        Text(
            text = placeholder,
            style = BalanceTextStyle,
            textAlign = TextAlign.End,
            modifier = Modifier.alpha(0f),
        )
        Text(
            text = text,
            style = BalanceTextStyle,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun AnimatedBalanceDemo() {
    var balance by remember { mutableIntStateOf(125_430) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Available Balance",
            fontSize = 16.sp,
            color = Color(0xFF9AA0A6),
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B1F27))
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedBalance(balance = balance)
        }

        Spacer(Modifier.height(32.dp))

        Button(onClick = { balance = (10_000..2_000_000).random() }) {
            Text("Refresh Balance")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = { balance += 5_000 }) {
            Text("Credit ₹5,000")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedBalanceDemoPreview() {
    AnimatedBalanceDemo()
}