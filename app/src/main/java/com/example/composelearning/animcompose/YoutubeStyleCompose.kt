package com.example.composelearning.animcompose

import android.app.Activity
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

// Light theme palette — white page, distinctly darker comment surface so the
// rounded top corners read clearly against the background.
private val PageBackground = Color(0xFFFFFFFF)
private val SurfaceColor = Color(0xFFE8E8E8)
private val ChipColor = Color(0xFFD9D9D9)
private val DividerColor = Color(0xFFCFCFCF)
private val TextPrimary = Color(0xFF0F0F0F)
private val TextSecondary = Color(0xFF606060)
private val LinkBlue = Color(0xFF065FD4)

private data class Comment(
    val author: String,
    val time: String,
    val text: String,
    val likes: String,
    val replies: Int,
    val avatar: String
)

private data class Suggestion(
    val title: String,
    val channel: String,
    val views: String,
    val time: String,
    val duration: String,
    val thumbnail: String
)

private val sampleComments = listOf(
    Comment(
        author = "@aravind_dev",
        time = "2 days ago",
        text = "The bezier breakdown at 3:14 finally made De Casteljau click for me. Thank you!",
        likes = "1.2K",
        replies = 24,
        avatar = "https://i.pravatar.cc/120?img=12"
    ),
    Comment(
        author = "@design_priya",
        time = "5 hours ago",
        text = "Designer here — wish more devs knew that 'Copy as SVG' gives them the exact control points. Saves so many handoff back-and-forths.",
        likes = "847",
        replies = 12,
        avatar = "https://i.pravatar.cc/120?img=47"
    ),
    Comment(
        author = "@kiran.codes",
        time = "1 day ago",
        text = "Built the playground in 20 minutes after watching this. The drag handles for the control points are SO satisfying.",
        likes = "312",
        replies = 5,
        avatar = "https://i.pravatar.cc/120?img=33"
    ),
    Comment(
        author = "@mintcompose",
        time = "3 days ago",
        text = "Tip: if you only have a screenshot, anchors at endpoints + control points along the tangent direction works 90% of the time.",
        likes = "204",
        replies = 8,
        avatar = "https://i.pravatar.cc/120?img=58"
    ),
    Comment(
        author = "@harsha_v",
        time = "6 days ago",
        text = "Watched this 3 times. The 'are control points given' question was the exact thing I was stuck on.",
        likes = "98",
        replies = 2,
        avatar = "https://i.pravatar.cc/120?img=15"
    )
)

private val sampleSuggestions = listOf(
    Suggestion(
        title = "Mastering Compose Canvas — Drawing curves, paths and gradients from scratch",
        channel = "Janajagruthi Maadhyama",
        views = "82K views",
        time = "1 month ago",
        duration = "17:14",
        thumbnail = "https://picsum.photos/seed/yt-canvas/640/360"
    ),
    Suggestion(
        title = "From Figma to Jetpack Compose — exporting SVG paths the right way",
        channel = "Compose Weekly",
        views = "45K views",
        time = "3 weeks ago",
        duration = "12:08",
        thumbnail = "https://picsum.photos/seed/yt-figma/640/360"
    ),
    Suggestion(
        title = "Animating bezier curves with AnimatedVectorDrawable vs Compose",
        channel = "Android Devs",
        views = "121K views",
        time = "2 months ago",
        duration = "9:42",
        thumbnail = "https://picsum.photos/seed/yt-anim/640/360"
    ),
    Suggestion(
        title = "Why your custom shape gets clipped — Outline.Generic explained",
        channel = "Pixel Lab",
        views = "27K views",
        time = "4 days ago",
        duration = "6:55",
        thumbnail = "https://picsum.photos/seed/yt-clip/640/360"
    )
)

@Composable
fun YouTubeScreen(onBack: () -> Unit = {}) {
    SetLightSystemBars()

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = dragOffset, label = "swipe-down")
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 160.dp.toPx() }
    val scope = rememberCoroutineScope()

    // Cooperate with the comment LazyColumn: when it's at the top and the user
    // drags down, redirect the leftover scroll into this screen's vertical offset.
    // When the user scrolls back up while the screen is pulled down, eat that
    // delta first to "unpull" before letting the list scroll.
    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (dragOffset > 0f && available.y < 0f) {
                    val consume = max(available.y, -dragOffset)
                    dragOffset += consume
                    return Offset(0f, consume)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f && source == NestedScrollSource.UserInput) {
                    dragOffset += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffset > dismissThreshold) {
                    onBack()
                    return available
                }
                if (dragOffset > 0f) {
                    animate(initialValue = dragOffset, targetValue = 0f) { value, _ ->
                        dragOffset = value
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, animatedOffset.roundToInt()) }
                .statusBarsPadding()
                .nestedScroll(nestedScroll)
                // Drag-to-dismiss for the upper non-scrolling area (channel/title/actions).
                // The LazyColumn handles the rest via nestedScroll above.
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dy ->
                            if (dragOffset + dy >= 0f) dragOffset += dy
                        },
                        onDragEnd = {
                            if (dragOffset > dismissThreshold) {
                                onBack()
                            } else {
                                scope.launch {
                                    animate(dragOffset, 0f) { v, _ -> dragOffset = v }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                animate(dragOffset, 0f) { v, _ -> dragOffset = v }
                            }
                        }
                    )
                }
        ) {
            VideoPlayerArea()
            ChannelHeaderRow()
            TitleSection()
            ActionButtonsRow()
            CommentsSection()
        }
    }
}

@Composable
private fun SetLightSystemBars() {
    val view = LocalView.current
    val context = LocalContext.current
    if (view.isInEditMode) return
    val activity = remember(context) { context.findActivity() } ?: return
    DisposableEffect(view) {
        val insets = WindowCompat.getInsetsController(activity.window, view)
        val previousStatus = insets.isAppearanceLightStatusBars
        val previousNav = insets.isAppearanceLightNavigationBars
        insets.isAppearanceLightStatusBars = true
        insets.isAppearanceLightNavigationBars = true
        onDispose {
            insets.isAppearanceLightStatusBars = previousStatus
            insets.isAppearanceLightNavigationBars = previousNav
        }
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var c = this
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

@Composable
private fun VideoPlayerArea() {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.fromUri(
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                )
            )
            prepare()
            playWhenReady = true
            volume = 0f
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ChannelHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://i.pravatar.cc/120?img=68",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(ChipColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Janajagruthi Maadhyama",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "284K subscribers",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = "Subscribe",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TitleSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Mastering Bezier Curves in Jetpack Compose — control points, De Casteljau, and Figma handoff",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "32K views • 2 days ago • #Compose #Bezier  ...more",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ActionButtonsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionPill(icon = Icons.Outlined.ThumbUp, label = "559")
        ActionPill(icon = Icons.Outlined.ThumbDown, label = "")
        ActionPill(icon = Icons.Outlined.Share, label = "Share")
        ActionPill(icon = Icons.Outlined.Download, label = "Download")
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        onClick = { },
        color = ChipColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun CommentsSection() {
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = SurfaceColor,
            shadowElevation = 10.dp
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = navInset + 16.dp)
            ) {
                item { CommentsHeader() }
                item { CommentInput() }
                item {
                    HorizontalDivider(
                        color = DividerColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(sampleComments) { comment ->
                    CommentItem(comment)
                }
                item {
                    HorizontalDivider(
                        color = DividerColor,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    Text(
                        text = "Up next",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                items(sampleSuggestions) { s ->
                    VideoSuggestionItem(s)
                }
            }
        }
    }
}

@Composable
private fun CommentsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Comments  ",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "31",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Collapse",
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CommentInput() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://i.pravatar.cc/120?img=5",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(50))
                .background(ChipColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            color = ChipColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Add a comment...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(50))
                .background(ChipColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "  •  ${comment.time}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(comment.likes, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Outlined.ThumbDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Reply",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (comment.replies > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "View ${comment.replies} replies",
                    color = LinkBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = TextSecondary,
            modifier = Modifier
                .padding(top = 6.dp)
                .size(18.dp)
        )
    }
}

@Composable
private fun VideoSuggestionItem(s: Suggestion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(86.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ChipColor)
        ) {
            AsyncImage(
                model = s.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = s.duration,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .height(86.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = s.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${s.channel} • ${s.views} • ${s.time}",
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = TextSecondary,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(18.dp)
        )
    }
}

@Preview(device = "id:pixel_5")
@Composable
fun YouTubeScreenPreview() {
    MaterialTheme {
        YouTubeScreen()
    }
}