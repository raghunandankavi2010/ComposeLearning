package com.example.composelearning.animcompose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalGridApi::class,
    ExperimentalFlexBoxApi::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun April2026FeaturesScreen(onBack: () -> Unit) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()
    var items by remember { mutableStateOf(List(10) { "Initial Item $it" }) }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            delay(1500)
            items = List(10) { "Refreshed Item ${System.currentTimeMillis() % 1000 + it}" }
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("April 2026 Updates") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = state,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Text(
                        "Foldable Video Sample (Adaptive)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    FoldableVideoSample()
                }

                item {
                    Text(
                        "Morphing Shapes (Graphics Shapes 1.1.0)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    MorphingShapeDemo()
                }

                item {
                    Text(
                        "Experimental Grid Layout",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    GridExample()
                }

                item {
                    Text(
                        "Experimental FlexBox Layout",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    FlexBoxWrapping()
                }

                item {
                    Text(
                        "Shared Element Demo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    SharedElementDemo()
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                            Spacer(Modifier.size(16.dp))
                            Text(item)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FoldableVideoSample() {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    
    Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    VideoPlayerPane()
                }
            },
            detailPane = {
                AnimatedPane {
                    VideoDescriptionPane()
                }
            }
        )
    }
}

@Composable
fun VideoPlayerPane() {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            setMediaItem(mediaItem)
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun VideoDescriptionPane() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Big Buck Bunny", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself. " +
            "When one sunny day three rodents rudely harass him, something snaps... " +
            "and the giant rabbit takes a revenge in a comical way!",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Text("Comments", style = MaterialTheme.typography.titleMedium)
        repeat(3) { index ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "User $index: This video is amazing!",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
fun GridExample() {
    Grid(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.DarkGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        config = {
            repeat(4) { column(0.25f) }
            repeat(2) { row(0.5f) }
            gap(16.dp)
        }
    ) {
        GridCard("Card 1 (rowSpan=2)", Color(0xFF6C63FF), Modifier.gridItem(rowSpan = 2))
        GridCard("Card 2 (colSpan=3)", Color(0xFFFF4D86), Modifier.gridItem(columnSpan = 3))
        GridCard("Card 3 (colSpan=2)", Color(0xFF2ED3B7), Modifier.gridItem(columnSpan = 2))
        GridCard("Card 4", Color(0xFFFFD479))
    }
}

@Composable
fun GridCard(text: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun FlexBoxWrapping() {
    FlexBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        config = {
            wrap(FlexWrap.Wrap)
            gap(8.dp)
        }
    ) {
        RoundedBox(Color.Red)
        RoundedBox(Color.Blue)
        RoundedBox(Color.Green, modifier = Modifier.width(350.dp).flex { grow(1.0f) })
        RoundedBox(Color.Magenta, modifier = Modifier.width(200.dp).flex { grow(0.7f) })
        RoundedBox(Color.Cyan, modifier = Modifier.width(200.dp).flex { grow(0.3f) })
    }
}

@Composable
fun RoundedBox(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(60.dp)
            .background(color, RoundedCornerShape(8.dp))
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementDemo() {
    var showDetail by remember { mutableStateOf(false) }
    SharedTransitionLayout {
        AnimatedContent(
            targetState = showDetail,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(500))
            },
            label = "shared_element"
        ) { targetShowDetail ->
            if (targetShowDetail) {
                DetailView(onBack = { showDetail = false }, sharedTransitionScope = this@SharedTransitionLayout, animatedVisibilityScope = this@AnimatedContent)
            } else {
                ListView(onItemClick = { showDetail = true }, sharedTransitionScope = this@SharedTransitionLayout, animatedVisibilityScope = this@AnimatedContent)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ListView(onItemClick: () -> Unit, sharedTransitionScope: androidx.compose.animation.SharedTransitionScope, animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope) {
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .sharedElement(
                    rememberSharedContentState(key = "item"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .clickable { onItemClick() }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailView(onBack: () -> Unit, sharedTransitionScope: androidx.compose.animation.SharedTransitionScope, animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp))
                    .clickable { onBack() }
            )
        }
    }
}

@Composable
fun MorphingShapeDemo() {
    var isSquare by remember { mutableStateOf(true) }
    val progress by animateFloatAsState(
        targetValue = if (isSquare) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "morph"
    )

    val square = remember { RoundedPolygon(4, rounding = androidx.graphics.shapes.CornerRounding(0.2f)) }
    val circle = remember { RoundedPolygon(12, rounding = androidx.graphics.shapes.CornerRounding(0.5f)) }
    val morph = remember(square, circle) { Morph(square, circle) }

    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
            .clickable { isSquare = !isSquare }
            .drawWithCache {
                val path = morph.toPath(progress).asComposePath()
                onDrawBehind {
                    drawPath(path, Color(0xFF6200EE))
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           // Content inside the morphing shape
        }
    }
}
