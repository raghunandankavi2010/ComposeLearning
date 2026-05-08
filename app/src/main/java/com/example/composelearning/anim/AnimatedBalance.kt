package com.example.composelearning.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnimatedBalance(
    balance: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 1200,
) {
    val animatedBalance = remember(balance) { Animatable(0f) }
    val targetBalance = balance.toFloat()

    LaunchedEffect(balance) {
        animatedBalance.animateTo(
            targetValue = targetBalance,
            animationSpec = tween(durationMillis = durationMillis),
        )
    }

    val displayBalance = animatedBalance.value.roundToInt()
    val formatted = remember(displayBalance) {
        NumberFormat.getNumberInstance(Locale.US).format(displayBalance)
    }

    Text(
        text = "₹ $formatted",
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier,
    )
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

        Button(onClick = { balance = (10_000..200_000).random() }) {
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