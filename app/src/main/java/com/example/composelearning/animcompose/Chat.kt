/*
 * Copyright 2026 Kyriakos Georgiopoulos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SupervisedUserCircle
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

//region --- Composition Locals ---

/**
 * Provides the [SharedTransitionScope] to children without passing it as a function parameter.
 * This avoids `VerifyError` crashes on certain Android runtimes caused by complex method signatures.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Provides the [AnimatedVisibilityScope] to children, required for Shared Element transitions.
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

//endregion

//region --- Animation Specs ---

/**
 * A custom spring configuration for Shared Element Transitions.
 * Low stiffness (100f) creates a slow, floaty movement.
 * Damping ratio (0.85f) ensures a soft, organic landing without excessive vibration.
 */
val playfulSpring = spring<Rect>(
    dampingRatio = 0.85f,
    stiffness = 100f
)

//endregion

//region --- Data Models ---

data class TabItem(
    val title: String,
    val color: Color
)

data class RecentMessage(
    val id: Int,
    val name: String,
    val message: String,
    val time: String,
    val isOnline: Boolean,
    val icon: ImageVector
)

data class ChatMessage(
    val id: Int,
    val text: String,
    val isFromMe: Boolean,
    val time: String
)

//endregion

//region --- Navigation ---

sealed class Screen {
    data object Home : Screen()
    data class Chat(val user: RecentMessage) : Screen()
}

//endregion

//region --- Root Composable ---

/**
 * The root navigation controller.
 * Handles state hoisting for list animations to ensure lists remain static when returning from a chat,
 * allowing the Shared Element Transition to find its target frame.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // -- State Hoisting --
    // We hoist the tab index so it persists when the Home screen is recreated after a back press.
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // We hoist the animation flag.
    // true = List items fly in (App start or Tab switch).
    // false = List items are static (Return from Chat).
    var shouldAnimateList by rememberSaveable { mutableStateOf(true) }

    // We apply a solid background to the root Layout to prevent the white window background
    // from flashing through during the double-transparent cross-fade.
    SharedTransitionLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1C1B2A))
    ) {
        AnimatedContent(
            targetState = currentScreen,
            label = "ScreenTransition",
            transitionSpec = {
                // Smooth cross-fade to match the slow shared element spring
                fadeIn(
                    animationSpec = tween(durationMillis = 600)
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = 600)
                )
            }
        ) { targetScreen ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this@AnimatedContent
            ) {
                when (targetScreen) {
                    is Screen.Home -> {
                        HeaderTabsFinal(
                            selectedIndex = selectedTabIndex,
                            shouldAnimate = shouldAnimateList,
                            onTabSelected = { newIndex ->
                                selectedTabIndex = newIndex
                                // Tab switch -> Trigger list entrance animation
                                shouldAnimateList = true
                            },
                            onChatSelected = { user ->
                                // Navigating away -> Freeze list state for return
                                shouldAnimateList = false
                                currentScreen = Screen.Chat(user)
                            }
                        )
                    }

                    is Screen.Chat -> {
                        ChatDetailScreen(
                            user = targetScreen.user,
                            onBack = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

//endregion

//region --- Screens ---

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatDetailScreen(
    user: RecentMessage,
    onBack: () -> Unit
) {
    // Safe unwrap of scopes. If null, the screen still renders, just without shared transitions.
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current ?: return

    val screenColor = Color(0xFF1C1B2A)
    val headerColor = Color(0xFF2B2939)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenColor)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // SHARED ELEMENT: AVATAR
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "avatar-${user.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> playfulSpring }
                        )
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3E3C4E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = user.icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // SHARED ELEMENT: NAME
            // Note: We use `sharedBounds` + `scaleToBounds` here.
            // This prevents the text from re-flowing (wrapping) as it animates, ensuring
            // the surname doesn't disappear during the transition.
            with(sharedTransitionScope) {
                Text(
                    text = user.name,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "name-${user.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> playfulSpring },
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // --- Chat Content ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(chatDummyData) { index, msg ->
                MessageBubble(msg = msg, index = index)
            }
        }

        // --- Input Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .background(Color(0xFF2B2939), RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach",
                tint = Color.White.copy(0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Write",
                color = Color.White.copy(0.3f),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF6C63FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderTabsFinal(
    selectedIndex: Int,
    shouldAnimate: Boolean,
    onTabSelected: (Int) -> Unit,
    onChatSelected: (RecentMessage) -> Unit
) {
    // Layout Constants
    val topSpace = 80.dp
    val tabTextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    val flareWidth = 56.dp
    val flareHeight = 36.dp
    val bottomCornerRadius = 26.dp

    val tabs = listOf(
        TabItem("Recents", Color(0xFF6C63FF)),
        TabItem("Favorites", Color(0xFFFF4D86)),
        TabItem("Groups", Color(0xFF2ED3B7))
    )

    // Animate header background color based on selection
    val headerColor by animateColorAsState(
        targetValue = tabs[selectedIndex].color,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 200f),
        label = "headerColor"
    )

    val density = LocalDensity.current
    val noRipple = remember { MutableInteractionSource() }

    // Dynamic tab sizing for the indicator
    var tabBounds by remember(tabs.size) { mutableStateOf(List(tabs.size) { Rect.Zero }) }
    val target = tabBounds.getOrNull(selectedIndex) ?: Rect.Zero

    val isFirst = selectedIndex == 0
    val isLast = selectedIndex == tabs.size - 1
    val hasStartFlare = !isFirst
    val hasEndFlare = !isLast

    // Calculate Indicator Position and Width
    val targetX =
        if (hasStartFlare) target.left.toDp(density) - flareWidth else target.left.toDp(density)
    val indicatorX by animateDpAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 200f),
        label = "X"
    )

    val widthAdjustment =
        (if (hasStartFlare) flareWidth else 0.dp) + (if (hasEndFlare) flareWidth else 0.dp)
    val indicatorW by animateDpAsState(
        targetValue = target.width.toDp(density) + widthAdjustment,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 200f),
        label = "W"
    )

    val screenColor = Color(0xFF1C1B2A)

    // Search Logic
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidth = containerSize.width.dp
    val maxSearchWidth = screenWidth - 24.dp

    // Playful Search Expansion Animation
    val searchWidthFraction by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f, // Jelly-like bounce
            stiffness = 200f     // Smooth speed
        ),
        label = "SearchWidth"
    )

    // Persist scroll states
    val recentsState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val favoritesState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val groupsState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenColor)
    ) {
        // --- Header Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topSpace)
                    .padding(horizontal = 8.dp)
            ) {
                // Add Button (Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .graphicsLayer {
                            // Shrink add button when search expands
                            val s = 1f - searchWidthFraction
                            scaleX = s
                            scaleY = s
                            alpha = s
                        }
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = noRipple,
                            indication = null
                        ) { /* Add Action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.requiredSize(28.dp)
                    )
                }

                // Search Bar (Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(60.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Background Pill
                    Box(
                        modifier = Modifier
                            .width(maxSearchWidth * searchWidthFraction)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Text Field
                    if (searchWidthFraction > 0.1f) {
                        Row(
                            modifier = Modifier
                                .width(maxSearchWidth * searchWidthFraction)
                                .height(50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .alpha(searchWidthFraction.coerceIn(0f, 1f)),
                                    textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                                    cursorBrush = SolidColor(Color.White),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchText.isEmpty()) {
                                            Text(
                                                text = "Search...",
                                                color = Color.White.copy(0.6f),
                                                fontSize = 20.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                            // Spacer to prevent text overlapping button area
                            Spacer(modifier = Modifier.width(60.dp))
                        }
                    }

                    LaunchedEffect(isSearchActive) {
                        if (isSearchActive) {
                            delay(100)
                            focusRequester.requestFocus()
                        }
                    }

                    // Search/Close Icon Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable(interactionSource = noRipple, indication = null) {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) searchText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // We use AnimatedContent with Scale+Fade to prevent visual overlapping
                        AnimatedContent(
                            targetState = isSearchActive,
                            transitionSpec = {
                                (scaleIn(animationSpec = tween(300)) + fadeIn(
                                    animationSpec = tween(
                                        300
                                    )
                                ))
                                    .togetherWith(
                                        scaleOut(animationSpec = tween(300)) + fadeOut(
                                            animationSpec = tween(
                                                300
                                            )
                                        )
                                    )
                            },
                            label = "IconAnim"
                        ) { active ->
                            Icon(
                                imageVector = if (active) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.requiredSize(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Tabs Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(screenColor)
        ) {
            // Animated Indicator (The "Gooey" Background)
            if (target.width > 0f) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorX, y = (-1).dp)
                        .width(indicatorW)
                        .height(57.dp)
                        .background(
                            color = headerColor,
                            shape = getUltraSmoothedEdgesShape(
                                flareWidth = with(density) { flareWidth.toPx() },
                                flareHeight = with(density) { flareHeight.toPx() },
                                cornerSize = with(density) { bottomCornerRadius.toPx() },
                                hasStartFlare = hasStartFlare,
                                hasEndFlare = hasEndFlare
                            )
                        )
                )
            }

            // Tab Text Items
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(interactionSource = noRipple, indication = null) {
                                onTabSelected(index)
                            }
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInParent()
                                tabBounds = tabBounds
                                    .toMutableList()
                                    .also { list ->
                                        list[index] = Rect(
                                            pos.x,
                                            pos.y,
                                            pos.x + coords.size.width,
                                            pos.y + coords.size.height
                                        )
                                    }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                            style = tabTextStyle
                        )
                    }
                }
            }
        }

        // --- Content Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(screenColor)
        ) {
            when (selectedIndex) {
                0 -> RecentsListShared(
                    items = dummyRecents,
                    state = recentsState,
                    onChatSelected = onChatSelected,
                    shouldAnimate = shouldAnimate
                )

                1 -> FavoritesListShared(
                    items = dummyFavorites,
                    state = favoritesState,
                    onChatSelected = onChatSelected,
                    shouldAnimate = shouldAnimate
                )

                2 -> GroupsListShared(
                    items = dummyGroups,
                    state = groupsState,
                    onChatSelected = onChatSelected,
                    shouldAnimate = shouldAnimate
                )
            }
            BottomNavBar(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

//endregion

//region --- Lists ---

@Composable
fun RecentsListShared(
    items: List<RecentMessage>,
    state: LazyListState,
    onChatSelected: (RecentMessage) -> Unit,
    shouldAnimate: Boolean
) {
    BoxWithConstraints {
        val startOffset = -maxWidth
        LazyColumnUI(items, state, onChatSelected, shouldAnimate, startOffset)
    }
}

@Composable
fun LazyColumnUI(
    items: List<RecentMessage>,
    state: LazyListState,
    onChatSelected: (RecentMessage) -> Unit,
    shouldAnimate: Boolean,
    startOffset: Dp
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items) { index, item ->
            // Animation Logic:
            // If shouldAnimate is true, we initialize at 0f/offset and animate to 1f/0.
            // If false, we initialize directly at 1f/0 (Static).
            val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
            val slideAnim =
                remember { Animatable(if (shouldAnimate) startOffset.value else 0f) }

            if (shouldAnimate) {
                LaunchedEffect(Unit) {
                    delay(index * 60L)
                    launch { alphaAnim.animateTo(1f, tween(400)) }
                    launch { slideAnim.animateTo(0f, spring(0.8f, Spring.StiffnessLow)) }
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = slideAnim.value.dp)
                    .alpha(alphaAnim.value)
            ) {
                SharedRecentItemRow(item = item, onChatSelected = onChatSelected)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesListShared(
    items: List<RecentMessage>,
    state: LazyListState,
    onChatSelected: (RecentMessage) -> Unit,
    shouldAnimate: Boolean
) {
    var favorites by remember { mutableStateOf(items) }

    LazyColumn(
        state = state,
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(favorites, key = { _, item -> item.id }) { index, item ->
            val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
            val offsetYAnim = remember { Animatable(if (shouldAnimate) -100f else 0f) }

            if (shouldAnimate) {
                LaunchedEffect(Unit) {
                    delay(index * 100L)
                    launch { alphaAnim.animateTo(1f, tween(500)) }
                    launch {
                        offsetYAnim.animateTo(
                            0f,
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = alphaAnim.value
                        translationY = offsetYAnim.value.dp.toPx()
                    }
            ) {
                DraggableFavoriteItemShared(
                    item = item,
                    onDelete = {
                        favorites = favorites.toMutableList().also { it.remove(item) }
                    },
                    onChatSelected = onChatSelected
                )
            }
        }
    }
}

@Composable
fun GroupsListShared(
    items: List<RecentMessage>,
    state: LazyListState,
    onChatSelected: (RecentMessage) -> Unit,
    shouldAnimate: Boolean
) {
    BoxWithConstraints {
        val startOffset = maxWidth
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
                val slideAnim =
                    remember { Animatable(if (shouldAnimate) startOffset.value else 0f) }

                if (shouldAnimate) {
                    LaunchedEffect(Unit) {
                        delay(index * 60L)
                        launch { alphaAnim.animateTo(1f, tween(400)) }
                        launch { slideAnim.animateTo(0f, spring(0.8f, Spring.StiffnessLow)) }
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(x = slideAnim.value.dp)
                        .alpha(alphaAnim.value)
                ) {
                    SharedRecentItemRow(item = item, onChatSelected = onChatSelected)
                }
            }
        }
    }
}

//endregion

//region --- Shared Item Components ---

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedRecentItemRow(
    item: RecentMessage,
    onChatSelected: (RecentMessage) -> Unit
) {
    // Safe Scope Access: We render the item even if shared transitions aren't active.
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val cardColor = Color(0xFF2B2939)
    // Custom interaction source to disable ripple effect
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(cardColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null // Ripple Removed
            ) { onChatSelected(item) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AVATAR (Shared Element)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF3E3C4E))
                .then(
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "avatar-${item.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> playfulSpring }
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // NAME (Shared Bounds + ScaleToBounds)
            Text(
                text = item.name,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                modifier = Modifier.then(
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "name-${item.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> playfulSpring },
                                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                    ContentScale.Fit,
                                    Alignment.CenterStart
                                )
                            )
                        }
                    } else Modifier
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.message,
                style = TextStyle(color = Color.White.copy(0.6f), fontSize = 14.sp),
                maxLines = 1
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = item.time,
                style = TextStyle(color = Color.White.copy(0.4f), fontSize = 12.sp)
            )
            if (item.isOnline) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF2ED3B7), CircleShape)
                )
            }
        }
    }
}

@Composable
fun DraggableFavoriteItemShared(
    item: RecentMessage,
    onDelete: () -> Unit,
    onChatSelected: (RecentMessage) -> Unit
) {
    val density = LocalDensity.current
    val revealSizeDp = 100.dp
    val maxRevealPx = with(density) { -revealSizeDp.toPx() }
    val snapThreshold = maxRevealPx / 2
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Reveal Background
        Box(
            modifier = Modifier
                .width(revealSizeDp)
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = (offsetX.value / maxRevealPx).coerceIn(0f, 1.2f)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(progress)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFF4D86))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Draggable Content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newVal = (offsetX.value + delta).coerceIn(maxRevealPx * 1.5f, 0f)
                        scope.launch { offsetX.snapTo(newVal) }
                    },
                    onDragStopped = {
                        val targetOffset = if (offsetX.value < snapThreshold) maxRevealPx else 0f
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = targetOffset,
                                animationSpec = spring(
                                    Spring.DampingRatioMediumBouncy,
                                    Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
        ) {
            SharedRecentItemRow(item = item, onChatSelected = onChatSelected)
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, index: Int) {
    val startOffsetX = if (msg.isFromMe) 200f else -200f
    val slideAnim = remember { Animatable(startOffsetX) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Delay helps content appear after shared transition settles
        delay(index * 100L + 300L)
        launch {
            slideAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = slideAnim.value
                alpha = alphaAnim.value
            },
        contentAlignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        if (msg.isFromMe) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                        else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                    )
                    .background(if (msg.isFromMe) Color(0xFF6C63FF) else Color(0xFF3E3C4E))
                    .padding(16.dp)
            ) {
                Text(
                    text = msg.text,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg.time,
                color = Color.White.copy(0.4f),
                fontSize = 12.sp
            )
        }
    }
}

//endregion

//region --- Utils & Bottom Bar ---

fun getUltraSmoothedEdgesShape(
    flareWidth: Float,
    flareHeight: Float,
    cornerSize: Float,
    hasStartFlare: Boolean,
    hasEndFlare: Boolean
) = GenericShape { size, _ ->
    val fw = flareWidth
    val fh = flareHeight
    val cs = cornerSize
    val w = size.width
    val h = size.height

    if (hasStartFlare) {
        moveTo(0f, 0f)
        cubicTo(fw * 0.8f, 0f, fw, fh * 0.4f, fw, fh)
        lineTo(fw, h - cs)
    } else {
        moveTo(0f, 0f)
        lineTo(0f, h - cs)
    }

    val lx = if (hasStartFlare) fw else 0f
    cubicTo(lx, h - (cs * 0.4f), lx + (cs * 0.4f), h, lx + cs, h)

    val rx = w - (if (hasEndFlare) fw else 0f)
    lineTo(rx - cs, h)
    cubicTo(rx - (cs * 0.4f), h, rx, h - (cs * 0.4f), rx, h - cs)

    if (hasEndFlare) {
        lineTo(rx, fh)
        cubicTo(rx, fh * 0.4f, rx + (fw * 0.2f), 0f, w, 0f)
    } else {
        lineTo(w, 0f)
    }
    close()
}

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(20.dp, RoundedCornerShape(36.dp), spotColor = Color.Black.copy(0.6f))
                .clip(RoundedCornerShape(36.dp))
                .background(Color(0xFF2B2939).copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF6C63FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubble,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = "Call",
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Preview
@Composable
fun AppPreview() {
    AppNavigation()
}

//endregion

// --- Dummy Data ---
val dummyRecents = listOf(
    RecentMessage(
        1,
        "Max Hall",
        "Hello Friend! How are you?",
        "08:30 pm",
        true,
        Icons.Rounded.Person
    ),
    RecentMessage(2, "Dan Martin", "Hi man! Do you know?...", "04:12 pm", true, Icons.Rounded.Face),
    RecentMessage(
        3,
        "Stephen Green",
        "Yes! I like it!",
        "02:05 pm",
        true,
        Icons.Rounded.EmojiEmotions
    ),
    RecentMessage(
        4,
        "Sarah Woodman",
        "How about my work?",
        "Yesterday",
        false,
        Icons.Rounded.SentimentSatisfied
    ),
    RecentMessage(5, "Peter Hopper", "At 5 pm", "01.22.201", false, Icons.Rounded.AccountCircle),
    RecentMessage(
        6,
        "Denis Ivanov",
        "Oh, no! Are you sure?",
        "01.16.201",
        false,
        Icons.Rounded.SupervisedUserCircle
    ),
    RecentMessage(7, "Alice Silver", "Hello Alex!", "01.12.201", false, Icons.Rounded.Face),
)
val dummyFavorites = listOf(
    RecentMessage(
        4,
        "Sarah Woodman",
        "How about my work?",
        "Yesterday",
        false,
        Icons.Rounded.SentimentSatisfied
    ),
    RecentMessage(5, "Peter Hopper", "At 5 pm", "01.22.201", false, Icons.Rounded.AccountCircle),
    RecentMessage(
        6,
        "Denis Ivanov",
        "Oh, no! Are you sure?",
        "01.16.201",
        false,
        Icons.Rounded.SupervisedUserCircle
    ),
    RecentMessage(7, "Alice Silver", "Hello Alex!", "01.12.201", false, Icons.Rounded.Face),
)
val dummyGroups = listOf(
    RecentMessage(
        10,
        "Design Team",
        "New mockups are ready!",
        "10:30 am",
        true,
        Icons.Rounded.Brush
    ),
    RecentMessage(
        11,
        "Weekend Trip",
        "Who is bringing the snacks?",
        "09:15 am",
        true,
        Icons.Rounded.DirectionsCar
    ),
    RecentMessage(
        12,
        "Family Group",
        "Mom: Call me when you can",
        "Yesterday",
        false,
        Icons.Rounded.Home
    ),
    RecentMessage(13, "Project Alpha", "Meeting delayed to 4 PM", "Mon", true, Icons.Rounded.Work),
    RecentMessage(14, "Gaming Squad", "Online tonight?", "Sun", false, Icons.Rounded.SportsEsports),
)

val chatDummyData = listOf(
    ChatMessage(1, "Hello Frank! How are you?", false, "12:30"),
    ChatMessage(2, "Hello I'm fine. Thanks! And you?", true, "12:28"),
    ChatMessage(3, "Fine! I have a question", false, "12:30"),
    ChatMessage(4, "Question?", true, "12:28"),
    ChatMessage(5, "How about my work?", false, "12:30"),
)