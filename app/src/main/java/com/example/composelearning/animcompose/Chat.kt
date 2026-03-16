package com.example.composelearning.animcompose

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
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

//endregion

//region --- Animation Specs ---

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

sealed class ChatScreen {
    data object Home : ChatScreen()
    data class Chat(val user: RecentMessage) : ChatScreen()
}

//endregion

//region --- Root Composable ---

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatAppNavigation() {
    var currentScreen by remember { mutableStateOf<ChatScreen>(ChatScreen.Home) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var shouldAnimateList by rememberSaveable { mutableStateOf(true) }

    SharedTransitionLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1C1B2A))
    ) {
        AnimatedContent(
            targetState = currentScreen,
            label = "ScreenTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
            }
        ) { targetScreen ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this@AnimatedContent
            ) {
                when (targetScreen) {
                    is ChatScreen.Home -> {
                        HeaderTabsFinal(
                            selectedIndex = selectedTabIndex,
                            shouldAnimate = shouldAnimateList,
                            onTabSelected = { newIndex ->
                                selectedTabIndex = newIndex
                                shouldAnimateList = true
                            },
                            onChatSelected = { user ->
                                shouldAnimateList = false
                                currentScreen = ChatScreen.Chat(user)
                            }
                        )
                    }
                    is ChatScreen.Chat -> {
                        ChatDetailScreen(
                            user = targetScreen.user,
                            onBack = { currentScreen = ChatScreen.Home }
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
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current ?: return

    val screenColor = Color(0xFF1C1B2A)
    val headerColor = Color(0xFF2B2939)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenColor)
    ) {
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

            with(sharedTransitionScope) {
                Text(
                    text = user.name,
                    style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
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
            Icon(Icons.Default.Search, "Search", tint = Color.White, modifier = Modifier.padding(end = 16.dp))
            Icon(Icons.Default.MoreVert, "Menu", tint = Color.White, modifier = Modifier.padding(end = 8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(chatDummyData) { index, msg ->
                MessageBubble(msg = msg, index = index)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .background(Color(0xFF2B2939), RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, "Attach", tint = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Write", color = Color.White.copy(0.3f), modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF6C63FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
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
    val flareWidth = 56.dp
    val flareHeight = 36.dp
    val bottomCornerRadius = 26.dp

    val tabs = listOf(
        TabItem("Recents", Color(0xFF6C63FF)),
        TabItem("Favorites", Color(0xFFFF4D86)),
        TabItem("Groups", Color(0xFF2ED3B7))
    )

    val headerColor by animateColorAsState(tabs[selectedIndex].color, spring(0.75f, 200f), label = "headerColor")
    val density = LocalDensity.current
    val noRipple = remember { MutableInteractionSource() }
    var tabBounds by remember(tabs.size) { mutableStateOf(List(tabs.size) { Rect.Zero }) }
    val target = tabBounds.getOrNull(selectedIndex) ?: Rect.Zero

    val indicatorX by animateDpAsState(
        targetValue = if (selectedIndex > 0) target.left.toDp(density) - flareWidth else target.left.toDp(density),
        animationSpec = spring(0.75f, 200f), label = "X"
    )
    val indicatorW by animateDpAsState(
        targetValue = target.width.toDp(density) + (if (selectedIndex > 0) flareWidth else 0.dp) + (if (selectedIndex < tabs.size - 1) flareWidth else 0.dp),
        animationSpec = spring(0.75f, 200f), label = "W"
    )

    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val screenWidth = LocalWindowInfo.current.containerSize.width.dp
    val searchWidthFraction by animateFloatAsState(if (isSearchActive) 1f else 0f, spring(0.7f, 200f), label = "SearchWidth")

    val recentsState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val favoritesState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val groupsState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Column(Modifier.fillMaxSize().background(Color(0xFF1C1B2A))) {
        Column(Modifier.fillMaxWidth().background(headerColor).statusBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 8.dp)) {
                Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp).graphicsLayer {
                    val s = 1f - searchWidthFraction
                    scaleX = s; scaleY = s; alpha = s
                }.size(60.dp).clip(CircleShape).clickable(noRipple, null) {}, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, "Add", tint = Color.White, modifier = Modifier.requiredSize(28.dp))
                }

                Box(Modifier.align(Alignment.CenterEnd).height(60.dp), contentAlignment = Alignment.CenterEnd) {
                    Box(Modifier.width((screenWidth - 24.dp) * searchWidthFraction).height(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.White.copy(0.25f)))
                    if (searchWidthFraction > 0.1f) {
                        BasicTextField(
                            value = searchText, onValueChange = { searchText = it },
                            modifier = Modifier.width((screenWidth - 24.dp) * searchWidthFraction).padding(start = 20.dp).focusRequester(focusRequester).alpha(searchWidthFraction),
                            textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                            cursorBrush = SolidColor(Color.White), singleLine = true
                        )
                    }
                    LaunchedEffect(isSearchActive) { if (isSearchActive) { delay(100); focusRequester.requestFocus() } }
                    Box(Modifier.size(60.dp).clip(CircleShape).clickable(noRipple, null) { isSearchActive = !isSearchActive; if (!isSearchActive) searchText = "" }, contentAlignment = Alignment.Center) {
                        Icon(if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search, null, tint = Color.White, modifier = Modifier.requiredSize(28.dp))
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF1C1B2A))) {
            if (target.width > 0f) {
                Box(Modifier.offset(indicatorX, (-1).dp).width(indicatorW).height(57.dp).background(Color(0xFF2B2939), getUltraSmoothedEdgesShape(with(density) { flareWidth.toPx() }, with(density) { flareHeight.toPx() }, with(density) { bottomCornerRadius.toPx() }, selectedIndex > 0, selectedIndex < tabs.size - 1)))
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, tab ->
                    Box(Modifier.weight(1f).fillMaxHeight().clickable(noRipple, null) { onTabSelected(index) }.onGloballyPositioned { coords ->
                        val pos = coords.positionInParent()
                        tabBounds = tabBounds.toMutableList().also { it[index] = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height) }
                    }, contentAlignment = Alignment.Center) {
                        Text(tab.title, color = if (index == selectedIndex) Color.White else Color.White.copy(0.5f), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (selectedIndex) {
                0 -> RecentsListShared(dummyRecents, recentsState, onChatSelected, shouldAnimate)
                1 -> FavoritesListShared(dummyFavorites, favoritesState, onChatSelected, shouldAnimate)
                2 -> GroupsListShared(dummyGroups, groupsState, onChatSelected, shouldAnimate)
            }
            BottomNavBar(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun RecentsListShared(items: List<RecentMessage>, state: LazyListState, onChatSelected: (RecentMessage) -> Unit, shouldAnimate: Boolean) {
    BoxWithConstraints { LazyColumnUI(items, state, onChatSelected, shouldAnimate, -maxWidth) }
}

@Composable
fun LazyColumnUI(items: List<RecentMessage>, state: LazyListState, onChatSelected: (RecentMessage) -> Unit, shouldAnimate: Boolean, startOffset: Dp) {
    LazyColumn(state = state, contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(items) { index, item ->
            val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
            val slideAnim = remember { Animatable(if (shouldAnimate) startOffset.value else 0f) }
            if (shouldAnimate) {
                LaunchedEffect(Unit) {
                    delay(index * 60L)
                    launch { alphaAnim.animateTo(1f, tween(400)) }
                    launch { slideAnim.animateTo(0f, spring(0.8f, Spring.StiffnessLow)) }
                }
            }
            Box(Modifier.offset(x = slideAnim.value.dp).alpha(alphaAnim.value)) { SharedRecentItemRow(item, onChatSelected) }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesListShared(items: List<RecentMessage>, state: LazyListState, onChatSelected: (RecentMessage) -> Unit, shouldAnimate: Boolean) {
    var favorites by remember { mutableStateOf(items) }
    LazyColumn(state = state, contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(favorites, key = { _, item -> item.id }) { index, item ->
            val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
            val offsetYAnim = remember { Animatable(if (shouldAnimate) -100f else 0f) }
            if (shouldAnimate) {
                LaunchedEffect(Unit) {
                    delay(index * 100L)
                    launch { alphaAnim.animateTo(1f, tween(500)) }
                    launch { offsetYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
                }
            }
            Box(Modifier.graphicsLayer { alpha = alphaAnim.value; translationY = offsetYAnim.value.dp.toPx() }) {
                DraggableFavoriteItemShared(item, { favorites = favorites.toMutableList().also { it.remove(item) } }, onChatSelected)
            }
        }
    }
}

@Composable
fun GroupsListShared(items: List<RecentMessage>, state: LazyListState, onChatSelected: (RecentMessage) -> Unit, shouldAnimate: Boolean) {
    BoxWithConstraints {
        val startOffset = maxWidth
        LazyColumn(state = state, contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(items) { index, item ->
                val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
                val slideAnim = remember { Animatable(if (shouldAnimate) startOffset.value else 0f) }
                if (shouldAnimate) {
                    LaunchedEffect(Unit) {
                        delay(index * 60L)
                        launch { alphaAnim.animateTo(1f, tween(400)) }
                        launch { slideAnim.animateTo(0f, spring(0.8f, Spring.StiffnessLow)) }
                    }
                }
                Box(Modifier.offset(x = slideAnim.value.dp).alpha(alphaAnim.value)) { SharedRecentItemRow(item, onChatSelected) }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedRecentItemRow(item: RecentMessage, onChatSelected: (RecentMessage) -> Unit) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(Color(0xFF2B2939), RoundedCornerShape(18.dp)).clickable(remember { MutableInteractionSource() }, null) { onChatSelected(item) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF3E3C4E)).then(if (sharedTransitionScope != null && animatedVisibilityScope != null) with(sharedTransitionScope) { Modifier.sharedElement(rememberSharedContentState("avatar-${item.id}"), animatedVisibilityScope, boundsTransform = { _, _ -> playfulSpring }) } else Modifier), contentAlignment = Alignment.Center) {
            Icon(item.icon, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, color = Color.White, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), maxLines = 1, modifier = Modifier.then(if (sharedTransitionScope != null && animatedVisibilityScope != null) with(sharedTransitionScope) { Modifier.sharedBounds(rememberSharedContentState("name-${item.id}"), animatedVisibilityScope, boundsTransform = { _, _ -> playfulSpring }, resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit, Alignment.CenterStart)) } else Modifier))
            Spacer(Modifier.height(4.dp))
            Text(item.message, color = Color.White.copy(0.6f), style = TextStyle(fontSize = 14.sp), maxLines = 1)
        }
        Column(modifier = Modifier.height(40.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
            Text(item.time, color = Color.White.copy(0.4f), style = TextStyle(fontSize = 12.sp))
            if (item.isOnline) Box(Modifier.size(10.dp).background(Color(0xFF2ED3B7), CircleShape))
        }
    }
}

@Composable
fun DraggableFavoriteItemShared(item: RecentMessage, onDelete: () -> Unit, onChatSelected: (RecentMessage) -> Unit) {
    val density = LocalDensity.current
    val revealSizeDp = 100.dp
    val maxRevealPx = with(density) { -revealSizeDp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Box(Modifier.width(revealSizeDp).height(72.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(72.dp).scale((offsetX.value / maxRevealPx).coerceIn(0f, 1.2f)).clip(RoundedCornerShape(18.dp)).background(Color(0xFFFF4D86)).clickable { onDelete() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, "Delete", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
        Box(Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }.draggable(orientation = Orientation.Horizontal,state = rememberDraggableState { delta -> scope.launch { offsetX.snapTo((offsetX.value + delta).coerceIn(maxRevealPx * 1.5f, 0f)) } }, onDragStopped = { scope.launch { offsetX.animateTo(if (offsetX.value < maxRevealPx / 2) maxRevealPx else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) } })) {
            SharedRecentItemRow(item, onChatSelected)
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, index: Int) {
    val slideAnim = remember { Animatable(if (msg.isFromMe) 200f else -200f) }
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(index * 100L + 300L); launch { slideAnim.animateTo(0f, spring(0.7f, Spring.StiffnessLow)) }; launch { alphaAnim.animateTo(1f, tween(400)) } }
    Box(Modifier.fillMaxWidth().graphicsLayer { translationX = slideAnim.value; alpha = alphaAnim.value }, contentAlignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start) {
            Box(Modifier.widthIn(max = 280.dp).clip(if (msg.isFromMe) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)).background(if (msg.isFromMe) Color(0xFF6C63FF) else Color(0xFF3E3C4E)).padding(16.dp)) {
                Text(msg.text, color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(msg.time, color = Color.White.copy(0.4f), fontSize = 12.sp)
        }
    }
}

fun getUltraSmoothedEdgesShape(fw: Float, fh: Float, cs: Float, hasStart: Boolean, hasEnd: Boolean) = GenericShape { size, _ ->
    val w = size.width; val h = size.height
    if (hasStart) { moveTo(0f, 0f); cubicTo(fw * 0.8f, 0f, fw, fh * 0.4f, fw, fh); lineTo(fw, h - cs) } else { moveTo(0f, 0f); lineTo(0f, h - cs) }
    val lx = if (hasStart) fw else 0f; cubicTo(lx, h - (cs * 0.4f), lx + (cs * 0.4f), h, lx + cs, h)
    val rx = w - (if (hasEnd) fw else 0f); lineTo(rx - cs, h); cubicTo(rx - (cs * 0.4f), h, rx, h - (cs * 0.4f), rx, h - cs)
    if (hasEnd) { lineTo(rx, fh); cubicTo(rx, fh * 0.4f, rx + (fw * 0.2f), 0f, w, 0f) } else { lineTo(w, 0f) }
    close()
}

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp), Alignment.BottomCenter) {
        Box(Modifier.fillMaxWidth().height(72.dp).shadow(20.dp, RoundedCornerShape(36.dp), spotColor = Color.Black.copy(0.6f)).clip(RoundedCornerShape(36.dp)).background(Color(0xFF2B2939).copy(0.95f)).border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(36.dp)), Alignment.Center) {
            Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF6C63FF)), Alignment.Center) { Icon(Icons.Rounded.ChatBubble, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                Icon(Icons.Outlined.Call, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(28.dp))
                Icon(Icons.Outlined.Person, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(28.dp))
                Icon(Icons.Outlined.Settings, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(28.dp))
            }
        }
    }
}

fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Preview @Composable fun AppPreview() { ChatAppNavigation() }

val dummyRecents = listOf(
    RecentMessage(1, "Max Hall", "Hello Friend! How are you?", "08:30 pm", true, Icons.Rounded.Person),
    RecentMessage(2, "Dan Martin", "Hi man! Do you know?...", "04:12 pm", true, Icons.Rounded.Face),
    RecentMessage(3, "Stephen Green", "Yes! I like it!", "02:05 pm", true, Icons.Rounded.EmojiEmotions),
    RecentMessage(4, "Sarah Woodman", "How about my work?", "Yesterday", false, Icons.Rounded.SentimentSatisfied),
    RecentMessage(5, "Peter Hopper", "At 5 pm", "01.22.201", false, Icons.Rounded.AccountCircle),
    RecentMessage(6, "Denis Ivanov", "Oh, no! Are you sure?", "01.16.201", false, Icons.Rounded.SupervisedUserCircle),
    RecentMessage(7, "Alice Silver", "Hello Alex!", "01.12.201", false, Icons.Rounded.Face),
)
val dummyFavorites = dummyRecents.take(4)
val dummyGroups = listOf(
    RecentMessage(10, "Design Team", "New mockups are ready!", "10:30 am", true, Icons.Rounded.Brush),
    RecentMessage(11, "Weekend Trip", "Who is bringing the snacks?", "09:15 am", true, Icons.Rounded.DirectionsCar),
    RecentMessage(12, "Family Group", "Mom: Call me when you can", "Yesterday", false, Icons.Rounded.Home),
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
