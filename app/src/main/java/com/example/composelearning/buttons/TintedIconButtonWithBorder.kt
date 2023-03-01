package com.example.composelearning.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TintedIconButtonWithBorder(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    strokeWidth: Dp = 2.dp,
    borderColor: Color,
    iconTintColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }

    val color = Modifier.background(color = Color.White)
    val border = Modifier.border(
        width = strokeWidth,
        color = borderColor,
        shape = CircleShape
    )
    Surface(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .clip(CircleShape)
            .then(border)
            .then(Modifier.background(Color.White)),
        color = Color.Transparent
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = color,
            tint = iconTintColor
        )
    }
}