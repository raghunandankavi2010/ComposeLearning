package com.example.composelearning.animcompose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Premium fluid tab bar with morphing indicators, per-tab colors, and spring physics.
 * Features gesture-based switching, shared element transitions, and glow effects.
 */

data class FluidTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
)

@Composable
fun FluidTabBarShowcase() {
    val tabs = remember {
        listOf(
            FluidTab(
                "Home",
                Icons.Rounded.Home,
                Color(0xFF6366F1), // Indigo
                Color(0xFF8B5CF6), // Purple
                Color(0xFFA78BFA)  // Light purple
            ),
            FluidTab(
                "Explore",
                Icons.Rounded.Explore,
                Color(0xFF06B6D4), // Cyan
                Color(0xFF3B82F6), // Blue
                Color(0xFF67E8F9)  // Light cyan
            ),
            FluidTab(
                "Favorites",
                Icons.Rounded.Favorite,
                Color(0xFFEC4899), // Pink
                Color(0xFFF43F5E), // Rose
                Color(0xFFFBCFE8)  // Light pink
            ),
            FluidTab(
                "Profile",
                Icons.Rounded.Person,
                Color(0xFF10B981), // Emerald
                Color(0xFF059669), // Green
                Color(0xFF6EE7B7)  // Light emerald
            )
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // Animated background color based on selected tab
    val animatedBackground by animateColorAsState(
        targetValue = tabs[selectedIndex].primaryColor.copy(alpha = 0.15f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedBackground,
                            Color(0xFF0F172A),
                            Color(0xFF020617)
                        )
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = tabs[selectedIndex].title,
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // The Fluid Tab Bar
            FluidTabBar(
                tabs = tabs,
                selectedIndex = selectedIndex,
                onTabSelected = { index ->
                    selectedIndex = index
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Content area with transition
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction } + fadeIn() + scaleIn(initialScale = 0.9f))
                        .togetherWith(slideOutHorizontally { -it * direction } + fadeOut() + scaleOut(targetScale = 0.9f))
                },
                label = "content"
            ) { index ->
                TabContent(
                    tab = tabs[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                )
            }
        }
    }
}

@Composable
fun FluidTabBar(
    tabs: List<FluidTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var tabWidth by remember { mutableFloatStateOf(0f) }
    var tabBarWidth by remember { mutableFloatStateOf(0f) }

    // Spring-based indicator position
    val indicatorOffset = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    val indicatorScale = remember { Animatable(1f) }

    // Gesture handling
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex, tabWidth) {
        if (tabWidth > 0) {
            val targetOffset = selectedIndex * tabWidth
            val targetWidth = tabWidth * 0.8f

            launch {
                indicatorOffset.animateTo(
                    targetValue = targetOffset,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                )
            }
            launch {
                indicatorWidth.animateTo(
                    targetValue = targetWidth,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                )
            }
            // Subtle bounce on selection
            launch {
                indicatorScale.snapTo(0.9f)
                indicatorScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .onSizeChanged { size ->
                tabBarWidth = size.width.toFloat()
                tabWidth = tabBarWidth / tabs.size
            }
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E293B))
            .pointerInput(tabs.size) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val draggedIndex = (indicatorOffset.value / tabWidth).roundToInt()
                            .coerceIn(0, tabs.size - 1)
                        if (draggedIndex != selectedIndex) {
                            onTabSelected(draggedIndex)
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        val newOffset = (selectedIndex * tabWidth + dragOffset)
                            .coerceIn(0f, tabBarWidth - tabWidth)
                        coroutineScope.launch {
                            indicatorOffset.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        // Indicator
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (indicatorOffset.value + (tabWidth - indicatorWidth.value) / 2).roundToInt(),
                        y = 0
                    )
                }
                .width(with(density) { indicatorWidth.value.toDp() })
                .fillMaxHeight()
                .padding(8.dp)
                .graphicsLayer {
                    scaleX = indicatorScale.value
                    scaleY = indicatorScale.value
                }
        ) {
            // Glow and Main Indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    tabs[selectedIndex].primaryColor.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                tabs[selectedIndex].primaryColor,
                                tabs[selectedIndex].secondaryColor
                            )
                        )
                    )
            )
        }

        // Tab items
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val distanceFromSelected = (index - selectedIndex).absoluteValue

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f - (distanceFromSelected * 0.1f),
                    animationSpec = spring(stiffness = 300f),
                    label = "iconScale"
                )

                val verticalOffset by animateDpAsState(
                    targetValue = if (isSelected) (-4).dp else 0.dp,
                    animationSpec = spring(stiffness = 400f),
                    label = "verticalOffset"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, verticalOffset.roundToPx()) }
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.White else tab.accentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + slideInHorizontally { it / 2 },
                        exit = fadeOut() + slideOutHorizontally { it / 2 }
                    ) {
                        Text(
                            text = tab.title,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabContent(
    tab: FluidTab,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tab.primaryColor.copy(alpha = 0.2f),
                        tab.secondaryColor.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) { index ->
            ContentCard(tab = tab, modifier = Modifier.fillMaxWidth().height(120.dp))
        }
    }
}

@Composable
fun ContentCard(
    tab: FluidTab,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(tab.primaryColor, tab.secondaryColor))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Box(Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF334155)))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF475569)))
            }
        }
    }
}

@Preview
@Composable
fun FluidTabBarPreview() {
    FluidTabBarShowcase()
}
